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

    /* 내부 예약 등록 서비스 - API로 구현하지 않음 */
    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE) // 다른 트랜잭션에서 읽기, 쓰기, 삭제 방지
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

        // check 1: 신청한 예약 엔트리의 정원 확인
        ReservationEntry entry = reRepo.findById(dto.getEntryId()).get(); // global하게 고유한 엔트리 id로 접근해서 처리해야 가장 빠를듯
        int capacity = entry.getCapacity();
        int reservedCount = entry.getReservedCount();
        int headcount = dto.getHeadcount();

        if (reservedCount + headcount > capacity) {
            log.warn("예약이 마감되었습니다.");
            // throw new CustomException(ErrorCode.RESERVATION_ALREADY_END); // V2라서 200 뜸
            // 이용자에게 예약 결과 전송
            notificationService.notifyFailureToMember(dto.getMemberCode(), dto.getStoreId());
            return;
        };
        
        // check 2: 해당 엔트리에 예약한 이력이 없을 시
        // 예약 이력은 DELETE 하지 않으므로 RESERVE_PENDING, CONFIRMED에 대해서만 조회
        // RESERVE_PENDING까지 포함하는 이유는, V1으로 예약했을 수도 있기 때문임.
        // +) List라서 Optional.isPresent()는 무조건 true -> !List.isEmpty()로 판단해야 함.
        if (!rRepo.findPreviousReservation(dto.getEntryId(), dto.getMemberCode(), dto.getStoreId()).get().isEmpty()) {
            log.warn("예약 이력이 존재합니다.");
            // throw new CustomException(ErrorCode.RESERVATION_EXISTS); // V2라서 200 뜸
            return;
        }

        // 모든 check를 통과했을 시 - 추후에 예외 처리 필요
        Store store = sRepo.findById(dto.getStoreId()).get();
        Member member = mRepo.findByMemberCode(dto.getMemberCode()).get();

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

        // 이용자에게 예약 결과 전송
        // 예약 성공했으므로 reservation 엔티티가 생성되었고, 따라서 sid가 아닌 rid를 넘겨주는 것이 맞음.
        notificationService.notifyReserveResultToMember(reservation.getReservationId(), "CONFIRMED");
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
