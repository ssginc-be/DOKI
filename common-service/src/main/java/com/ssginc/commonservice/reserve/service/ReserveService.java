package com.ssginc.commonservice.reserve.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssginc.commonservice.exception.CustomException;
import com.ssginc.commonservice.exception.ErrorCode;
import com.ssginc.commonservice.member.model.Member;
import com.ssginc.commonservice.member.model.MemberRepository;
import com.ssginc.commonservice.notification.service.NotificationService;
import com.ssginc.commonservice.reserve.dto.ReserveRequestDto;
import com.ssginc.commonservice.reserve.model.*;
import com.ssginc.commonservice.store.model.Store;
import com.ssginc.commonservice.store.model.StoreRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Queue-ri
 */

@Slf4j
@RequiredArgsConstructor
@Service
public class ReserveService {
    private final ObjectMapper objectMapper;
    private final ReservationRepository rRepo;
    private final ReservationLogRepository rlRepo;
    private final ReservationEntryRepository reRepo;
    private final MemberRepository mRepo;
    private final StoreRepository sRepo;

    private final EntityManager entityManager;

    private final NotificationService notificationService;

    /************************************************************************************
        [V2 자동승인] 내부 예약 등록 서비스
            1. API로 구현하지 않음
            2. Kafka Listener
    */
    @Transactional
    @KafkaListener(topics = "doki-reserve", groupId = "doki")
    public void consumeAndReserve(String message) {
        log.info(message);

        // 예약 신청 메시지를 dto로 변환
        ReserveRequestDto dto = null;
        try {
            dto = objectMapper.readValue(message, ReserveRequestDto.class);
        } catch (Exception e) {
            log.error("예약 요청 dto 변환 실패");
            throw new CustomException(ErrorCode.SOMETHING_WENT_WRONG);
        }

        // check 2: 해당 엔트리에 예약한 이력이 없을 시
        // 예약 이력은 DELETE 하지 않으므로 RESERVE_PENDING, CONFIRMED에 대해서만 조회
        // RESERVE_PENDING까지 포함하는 이유는, V1으로 예약했을 수도 있기 때문임.
        // +) List라서 Optional.isPresent()는 무조건 true -> !List.isEmpty()로 판단해야 함.
        checkIfReservationExists(dto);

        // check 1: 신청한 예약 엔트리의 정원 확인
        // entry -> 신청한 예약 엔트리 데이터
        ReservationEntry entry = checkEntryCapacityAndUpdateStatus(dto, true);

        // 모든 check를 통과했을 시 - 추후에 예외 처리 필요
        Store store = sRepo.findById(dto.getStoreId()).get();
        Member member = mRepo.findByMemberCode(dto.getMemberCode()).get();
        int headcount = dto.getHeadcount();

        Reservation reservation = Reservation.builder()
                .store(store)
                .member(member)
                .reservationEntry(entry)
                .reservedDateTime(dto.getReservedDateTime())
                .headcount(headcount)
                .reservationStatus(Reservation.ReservationStatus.CONFIRMED) // v2는 신청하면 즉시 예약 승인됨
                .build();

        // save하면서 생성된 auto_increment id 값을 알아야 하므로, save 대신 entity manager의 persist를 사용
        // id 값을 명시적으로 넣지 않고, DB에 위임하기 때문에
        // 영속화가 일어날 때 INSERT 쿼리가 날라감.
        // 따라서 entityManager.persist() 호출해서 영속화 한 후, 이 때 id 값 가져오고
        // 그 후에 transaction.commit() 하면 됨. (이는 어노테이션으로 대체)
        // rRepo.save(reservation);

        entityManager.persist(reservation); // 이 statement 이후부터 id 값 알 수 있음

        // reservation log 생성 및 등록
        ReservationLog rlog = ReservationLog.builder()
                .reservation(reservation)
                .reserveMethod(ReservationLog.ReserveMethod.V2) // v2 API로 예약함
                .reservationStatus(ReservationLog.ReservationStatus.CONFIRMED) // 즉시 예약 승인됨
                .build();
        
        rlRepo.save(rlog);

        // 추후에 예외처리 필요
        // headcount만큼 예약한 엔트리의 예약자 수 업데이트
        entry.setReservedCount(entry.getReservedCount() + dto.getHeadcount());
        reRepo.save(entry);

        // 운영자 -> 이용자에게 예약 확정 알림
        // 예약 성공했으므로 reservation 엔티티가 생성되었고, 따라서 sid가 아닌 rid를 넘겨주는 것이 맞음.
        notificationService.notifyReserveResultToMember(reservation.getReservationId(), "CONFIRMED");

        // 이용자 -> 운영자에게 예약 자동 승인 알림
        // 예약 V2는 자동 승인됨.
        notificationService.notifyReserveRequestToManager(store.getMember().getMemberCode(), dto.getReservedDateTime(), "AUTO_CONFIRMED");
    }


    /************************************************************************************
        [V1 직접승인] 예약 요청 시 승인 대기 상태의 예약을 생성하는 함수
            1. API로 연결되어 있음.
            2. 해당 Reservation의 status는 RESERVE_PENDING
    */
    @Transactional
    public ResponseEntity<?> createPendingReservation(ReserveRequestDto dto) {
        // 상세 로직 설명은 V2 자동승인(consumeAndReserve)의 주석 참고

        // check 2: 해당 엔트리에 예약한 이력이 없을 시
        checkIfReservationExists(dto);

        // check 1: 신청한 예약 엔트리의 정원 확인
        ReservationEntry entry = checkEntryCapacityAndUpdateStatus(dto, false);

        // 모든 check를 통과했을 시 - 추후에 예외 처리 필요
        Store store = sRepo.findById(dto.getStoreId()).get();
        Member member = mRepo.findByMemberCode(dto.getMemberCode()).get();
        int headcount = dto.getHeadcount();

        Reservation reservation = Reservation.builder()
                .store(store)
                .member(member)
                .reservationEntry(entry)
                .reservedDateTime(dto.getReservedDateTime())
                .headcount(headcount)
                .reservationStatus(Reservation.ReservationStatus.RESERVE_PENDING) // v1은 신청하면 승인 대기 처리
                .build();

        // INSERT시 생성되는 auto_increment id 값을 알아야 하므로, save 대신 entity manager의 persist를 사용
        entityManager.persist(reservation); // 이 statement 이후부터 id 값 알 수 있음

        // reservation log 생성 및 등록
        ReservationLog rlog = ReservationLog.builder()
                .reservation(reservation)
                .reserveMethod(ReservationLog.ReserveMethod.V1) // v1 API로 예약함
                .reservationStatus(ReservationLog.ReservationStatus.RESERVE_PENDING) // 예약 승인 대기
                .build();

        rlRepo.save(rlog);

        // 추후에 예외처리 필요
        // v1 예약은 바로 확정되지 않기에, reserved_count가 영향받지 않음.
        reRepo.save(entry);

        // 운영자 -> 이용자에게 예약 신청 완료 알림
        // 예약 성공했으므로 reservation 엔티티가 생성되었고, 따라서 sid가 아닌 rid를 넘겨주는 것이 맞음.
        notificationService.notifyReserveResultToMember(reservation.getReservationId(), "RESERVE_PENDING");

        // 이용자 -> 운영자에게 예약 승인 요청 알림
        notificationService.notifyReserveRequestToManager(store.getMember().getMemberCode(), dto.getReservedDateTime(), "CONFIRM_REQUEST");

        return ResponseEntity.ok().build();
    }


    /************************************************************************************
        [V1 / V2 공통] check 1 함수
            1. 예약 요청한 엔트리의 정원 확인
            2. 예약 요청한 엔트리의 상태 업데이트 (OPEN, CLOSED)
    */
    public ReservationEntry checkEntryCapacityAndUpdateStatus(ReserveRequestDto dto, boolean updateStatus) {
        ReservationEntry entry = reRepo.findById(dto.getEntryId()).get(); // global하게 고유한 엔트리 id로 접근해서 처리해야 가장 빠를듯
        int capacity = entry.getCapacity();
        int reservedCount = entry.getReservedCount();
        int headcount = dto.getHeadcount();

        if (reservedCount + headcount > capacity) {
            log.warn("예약이 마감되었습니다.");
            // 이용자에게 예약 결과 전송
            notificationService.notifyFailureToMember(dto.getMemberCode(), dto.getStoreId());
            // 참고: V2는 CustomException 적용되지 않고 예약 요청 단에서 200 OK 반환됨.
            throw new CustomException(ErrorCode.RESERVATION_ALREADY_END);
        }
        else if (updateStatus && (reservedCount + headcount == capacity)) {
            // 정원 한도에 도달했으므로 예약 엔트리 status를 CLOSED로 업데이트해야 함.
            entry.setEntryStatus(ReservationEntry.EntryStatus.CLOSED);
        }

        return entry;
    }


    /************************************************************************************
        [V1 / V2 공통] check 2 함수
            1. 예약 요청한 엔트리에 이전의 예약 이력 존재하는지 검사
    */
    public void checkIfReservationExists(ReserveRequestDto dto) {
        if (!rRepo.findPreviousReservation(dto.getEntryId(), dto.getMemberCode(), dto.getStoreId()).isEmpty()) {
            log.warn("예약 이력이 존재합니다.");
            // 참고: V2는 CustomException 적용되지 않고 예약 요청 단에서 200 OK 반환됨.
            throw new CustomException(ErrorCode.RESERVATION_EXISTS);
        }
    }


    /* 이용자의 예약 내역 조회 - API로 구현하지 않음 */
    public List<Reservation> getMemberReservations(Long memberCode) {
        return rRepo.findByMember_MemberCode(memberCode);
    }

    /* 특정 팝업스토어 예약 내역 조회 */
    // 내부에서만 사용 - API로 구현하지 않음
    public List<Reservation> getStoreReservations(Long storeId) {
        return rRepo.findByStore_StoreId(storeId);
    }
}
