package com.ssginc.commonservice.member.service;

import com.ssginc.commonservice.exception.CustomException;
import com.ssginc.commonservice.exception.ErrorCode;
import com.ssginc.commonservice.member.model.Member;
import com.ssginc.commonservice.member.model.MemberRepository;
import com.ssginc.commonservice.reserve.model.Reservation;
import com.ssginc.commonservice.reserve.model.ReservationLog;
import com.ssginc.commonservice.reserve.model.ReservationLogRepository;
import com.ssginc.commonservice.reserve.model.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author Queue-ri
 */

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {
    /*
        회원 정보 조회, 예약 취소 요청
    */
    private final MemberRepository mRepo;
    private final ReservationRepository rRepo;
    private final ReservationLogRepository rlRepo;


    /* 회원 정보 조회 - 내부에서만 사용하고 API는 없음 */
    public Member getMemberInfo(Long memberCode) {
        Optional<Member> optMember = mRepo.findById(memberCode);

        if (optMember.isEmpty()) {
            log.info("요청 code의 회원 조회 결과 없음.");
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        return optMember.get();
    }

    /* 팝업스토어 예약 취소 요청 */
    public ResponseEntity<?> requestReservationCancel(Long rid) {
        Optional<Reservation> optReservation = rRepo.findById(rid);
        if (optReservation.isEmpty()) {
            log.error("요청 id의 예약 조회 결과 없음");
            throw new CustomException(ErrorCode.RESERVATION_NOT_FOUND);
        }

        // 예약 내역 상태 업데이트
        Reservation reservation = optReservation.get();
        reservation.setReservationStatus(Reservation.ReservationStatus.CANCEL_PENDING); // UPDATE

        // 예약 상태변경 로그 작성
        ReservationLog rlog = ReservationLog.builder()
                .reservation(reservation)
                .reserveMethod(ReservationLog.ReserveMethod.V1)
                .reservationStatus(ReservationLog.ReservationStatus.CANCEL_PENDING)
                .build();
        rlRepo.save(rlog);

        return ResponseEntity.ok().build();
    }
}
