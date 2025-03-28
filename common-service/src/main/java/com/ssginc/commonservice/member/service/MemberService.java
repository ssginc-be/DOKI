package com.ssginc.commonservice.member.service;

import com.ssginc.commonservice.exception.CustomException;
import com.ssginc.commonservice.exception.ErrorCode;
import com.ssginc.commonservice.member.dto.MyReservationResponseDto;
import com.ssginc.commonservice.member.model.Member;
import com.ssginc.commonservice.member.model.MemberRepository;
import com.ssginc.commonservice.notification.service.NotificationService;
import com.ssginc.commonservice.reserve.model.Reservation;
import com.ssginc.commonservice.reserve.model.ReservationLog;
import com.ssginc.commonservice.reserve.model.ReservationLogRepository;
import com.ssginc.commonservice.reserve.model.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Queue-ri
 */

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {
    /*
        회원 정보 조회, 나의 예약 조회, 예약 취소 요청
    */
    private final MemberRepository mRepo;
    private final ReservationRepository rRepo;
    private final ReservationLogRepository rlRepo;
    private final NotificationService notificationService;


    /* 회원 정보 조회 - 내부에서만 사용하고 API는 없음 */
    public Member getMemberInfo(Long memberCode) {
        Optional<Member> optMember = mRepo.findById(memberCode);

        if (optMember.isEmpty()) {
            log.info("요청 code의 회원 조회 결과 없음.");
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        return optMember.get();
    }

    /* 나의 예약 조회 */
    public ResponseEntity<?> getMyReservationList(Long code) {
        List<Reservation> reservationList = rRepo.findByMember_MemberCodeOrderByCreatedAtDesc(code);

        // entity -> dto 변환
        List<MyReservationResponseDto> dtoList = new ArrayList<>();
        for (Reservation reservation : reservationList) {
            MyReservationResponseDto dto = MyReservationResponseDto.builder()
                    .reservationId(reservation.getReservationId())
                    .storeName(reservation.getStore().getStoreName())
                    .reservedDate(reservation.getReservedDateTime().toLocalDate())
                    .reservedTime(reservation.getReservedDateTime().toLocalTime())
                    .reservationStatus(reservation.getReservationStatus().toString())
                    .build();
            
            dtoList.add(dto);
        }

        return ResponseEntity.ok(dtoList);
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

        // 이용자 -> 운영자 예약 취소 요청 SSE 알림
        notificationService.notifyReserveRequestToManager(
                reservation.getStore().getMember().getMemberCode(),
                reservation.getReservedDateTime(),
                "CANCEL_REQUEST"
        );

        return ResponseEntity.ok().build();
    }
}
