package com.ssginc.commonservice.backdoor;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Queue-ri
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class TestReserveService {
    /*
        예약 V2 동시성 테스트용 서비스
    */
    private final ReservationLogRepository rlRepo;
    private final ReservationEntryRepository reRepo;
    private final MemberRepository mRepo;
    private final StoreRepository sRepo;

    private final EntityManager entityManager;

    private final NotificationService notificationService;

    /************************************************************************************
        [V2 자동승인] 내부 예약 등록 서비스
            1. API로 구현하지 않음
    */
    @Transactional
    public void reserve(ReserveRequestDto dto, int testMode) {
        // check 1: 신청한 예약 엔트리의 정원 확인
        ReservationEntry entry = checkEntryCapacityAndUpdateStatus(dto, true, testMode);

        // check를 통과했을 시 - 추후에 예외 처리 필요
        Store store = sRepo.findById(dto.getStoreId()).get();
        Member member = mRepo.findByMemberCode(3L).get();
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

    @Transactional
    synchronized public void synchronizedReserve(ReserveRequestDto dto, int testMode) {
        // check 1: 신청한 예약 엔트리의 정원 확인
        ReservationEntry entry = checkEntryCapacityAndUpdateStatus(dto, true, testMode);

        // check를 통과했을 시 - 추후에 예외 처리 필요
        Store store = sRepo.findById(dto.getStoreId()).get();
        Member member = mRepo.findByMemberCode(3L).get();
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
        [V1 / V2 공통] check 1 함수
            1. 예약 요청한 엔트리의 정원 확인
            2. 예약 요청한 엔트리의 상태 업데이트 (OPEN, CLOSED)
    */
    public ReservationEntry checkEntryCapacityAndUpdateStatus(ReserveRequestDto dto, boolean updateStatus, int testMode) {
        ReservationEntry entry;
        if (testMode == 1)
            entry = reRepo.findById(dto.getEntryId()).get(); // Pessimistic Lock mode
        else
            entry = reRepo.findByIdNoLock(dto.getEntryId()).get(); // REPEATABLE_READ mode
        int capacity = entry.getCapacity();
        int reservedCount = entry.getReservedCount();
        int headcount = dto.getHeadcount();

        if (reservedCount + headcount > capacity) {
            log.info("reservedCount({}) + headcount({}) > capacity({})", reservedCount, headcount, capacity);
            log.info("memberCode: {} | headcount:{}", dto.getMemberCode(), dto.getHeadcount());
            log.warn("예약이 마감되었습니다.");
            // 이용자에게 예약 결과 전송
            notificationService.notifyFailureToMember(dto.getMemberCode(), dto.getStoreId());
            // 참고: V2는 CustomException 적용되지 않고 예약 요청 단에서 200 OK 반환됨.
            throw new CustomException(ErrorCode.RESERVATION_ALREADY_END);
        }
        else if (updateStatus && (reservedCount + headcount == capacity)) {
            log.info("reservedCount({}) + headcount({}) == capacity({})", reservedCount, headcount, capacity);
            log.info("memberCode: {} | headcount:{}", dto.getMemberCode(), dto.getHeadcount());
            // 정원 한도에 도달했으므로 예약 엔트리 status를 CLOSED로 업데이트해야 함.
            entry.setEntryStatus(ReservationEntry.EntryStatus.CLOSED);
        }

        return entry;
    }

}
