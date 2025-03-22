package com.ssginc.commonservice.backdoor.service;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Queue-ri
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class PerfTestReserveService {

    private final ObjectMapper objectMapper;
    private final ReservationLogRepository rlRepo;
    private final ReservationEntryRepository reRepo;
    private final MemberRepository mRepo;
    private final StoreRepository sRepo;

    private final EntityManager entityManager;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /* [V2 자동승인] 예약 요청 시 Kafka 메시지 발행하는 함수 */
    public ResponseEntity<?> sendMessage(ReserveRequestDto dto) {
        String message = null;
        try {
            message = objectMapper.writeValueAsString(dto);
        } catch (Exception e) {
            log.error("예약 요청 message 변환 실패");
            throw new CustomException(ErrorCode.SOMETHING_WENT_WRONG);
        }
        log.info(message);
        kafkaTemplate.send("doki-reserve", message);

        return ResponseEntity.ok().build();
    }

    /************************************************************************************
     [V2 자동승인] 내부 예약 등록 서비스
         1. API로 구현하지 않음
         2. Kafka Listener
     */
    @Transactional
    @KafkaListener(topics = "doki-reserve", groupId = "doki")
    public void consumeAndReserve(String message) {
        log.info(message);
//
//        // 예약 신청 메시지를 dto로 변환
//        ReserveRequestDto dto = null;
//        try {
//            dto = objectMapper.readValue(message, ReserveRequestDto.class);
//        } catch (Exception e) {
//            log.error("예약 요청 dto 변환 실패");
//            throw new CustomException(ErrorCode.SOMETHING_WENT_WRONG);
//        }
//
//        // check 1: 신청한 예약 엔트리의 정원 확인
//        // entry -> 신청한 예약 엔트리 데이터
//        ReservationEntry entry = checkEntryCapacityAndUpdateStatus(dto, true);
//
//        // 모든 check를 통과했을 시 - 추후에 예외 처리 필요
//        Store store = sRepo.findById(dto.getStoreId()).get();
//        Member member = mRepo.findByMemberCode(dto.getMemberCode()).get();
//        int headcount = dto.getHeadcount();
//
//        Reservation reservation = Reservation.builder()
//                .store(store)
//                .member(member)
//                .reservationEntry(entry)
//                .reservedDateTime(dto.getReservedDateTime())
//                .headcount(headcount)
//                .reservationStatus(Reservation.ReservationStatus.CONFIRMED) // v2는 신청하면 즉시 예약 승인됨
//                .build();
//
//        entityManager.persist(reservation); // 이 statement 이후부터 id 값 알 수 있음
//
//        // reservation log 생성 및 등록
//        ReservationLog rlog = ReservationLog.builder()
//                .reservation(reservation)
//                .reserveMethod(ReservationLog.ReserveMethod.V2) // v2 API로 예약함
//                .reservationStatus(ReservationLog.ReservationStatus.CONFIRMED) // 즉시 예약 승인됨
//                .build();
//
//        rlRepo.save(rlog);
//
//        // headcount만큼 예약한 엔트리의 예약자 수 업데이트
//        entry.setReservedCount(entry.getReservedCount() + dto.getHeadcount());
//        reRepo.save(entry);

    }


    /************************************************************************************
     [V1 직접승인] 예약 요청 시 승인 대기 상태의 예약을 생성하는 함수
         1. API로 연결되어 있음.
         2. 해당 Reservation의 status는 RESERVE_PENDING
     */
    @Transactional
    public ResponseEntity<?> createPendingReservation(ReserveRequestDto dto) {
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

        return ResponseEntity.ok().build();
    }


    /************************************************************************************
     [V1 / V2 공통] check 1 함수
         1. 예약 요청한 엔트리의 정원 확인
         2. 예약 요청한 엔트리의 상태 업데이트 (OPEN, CLOSED)
     */
    public ReservationEntry checkEntryCapacityAndUpdateStatus(ReserveRequestDto dto, boolean updateStatus) {
        ReservationEntry entry = reRepo.findById(dto.getEntryId()).get();
        int capacity = entry.getCapacity();
        int reservedCount = entry.getReservedCount();
        int headcount = dto.getHeadcount();

        if (reservedCount + headcount > capacity) {
            log.warn("예약이 마감되었습니다.");
            // 참고: V2는 CustomException 적용되지 않고 예약 요청 단에서 200 OK 반환됨.
            throw new CustomException(ErrorCode.RESERVATION_ALREADY_END);
        }
        else if (updateStatus && (reservedCount + headcount == capacity)) {
            // 정원 한도에 도달했으므로 예약 엔트리 status를 CLOSED로 업데이트해야 함.
            entry.setEntryStatus(ReservationEntry.EntryStatus.CLOSED);
        }

        return entry;
    }
}
