package com.ssginc.commonservice.notification.service;

import com.ssginc.commonservice.exception.CustomException;
import com.ssginc.commonservice.exception.ErrorCode;
import com.ssginc.commonservice.notification.controller.NotificationController;
import com.ssginc.commonservice.notification.domain.Notification;
import com.ssginc.commonservice.notification.domain.NotificationRepository;
import com.ssginc.commonservice.reserve.model.Reservation;
import com.ssginc.commonservice.reserve.model.ReservationRepository;
import com.ssginc.commonservice.store.model.Store;
import com.ssginc.commonservice.store.model.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Queue-ri
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final ReservationRepository rRepo;
    private final NotificationRepository nRepo;
    private final StoreRepository sRepo;

    /* 로그인 유저 대상 SSE 연결 */
    public SseEmitter subscribe(Long memberCode) {

        // 1. 현재 클라이언트를 위한 sseEmitter 객체 생성
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE);

        // 2. 연결
        try {
            sseEmitter.send(SseEmitter.event().name("connect"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 3. 저장
        NotificationController.sseEmitters.put(memberCode, sseEmitter);

        // 4. 연결 종료 처리
        sseEmitter.onCompletion(() -> NotificationController.sseEmitters.remove(memberCode));	// sseEmitter 연결이 완료될 경우
        sseEmitter.onTimeout(() -> NotificationController.sseEmitters.remove(memberCode));		// sseEmitter 연결에 타임아웃이 발생할 경우
        sseEmitter.onError((e) -> NotificationController.sseEmitters.remove(memberCode));		// sseEmitter 연결에 오류가 발생할 경우

        return sseEmitter;
    }


    /* 특정 member의 전체 알림 조회 - 서비스 정책 상 조회된다는 것 == 아직 읽지 않음 */
    public List<Notification> getAll(Long memberCode) {
        return nRepo.findAllByMember_MemberCode(memberCode);
    }


    /* [INTERNAL] 예약 결과 알림 - 운영자 to 이용자 */
    // rid: 운영자가 승인/거절/취소한 예약의 id
    // 팝업스토어 id가 아닌 예약 id를 받는 것이 맞음
    public void notifyReserveResultToMember(Long rid, String resultStatus) {
        // 추후에 에러핸들링 필요
        Reservation reservation = rRepo.findById(rid).get();
        Long memberCode = reservation.getMember().getMemberCode(); // 해당 예약의 이용자

        if (NotificationController.sseEmitters.containsKey(memberCode)) {
            SseEmitter sseEmitter = NotificationController.sseEmitters.get(memberCode);
            try {
                String storeName = reservation.getStore().getStoreName(); // 이용자가 예약 관련 요청을 보낸 팝업스토어명

                String resultStr = ""; // 예약 결과 알림 메시지 내용
                if (resultStatus.equals("CONFIRMED")) resultStr = "예약이 승인되었습니다.";
                else if (resultStatus.equals("REFUSED")) resultStr = "예약이 거절되었습니다.";
                else if (resultStatus.equals("CANCELED")) resultStr = "예약이 취소되었습니다.";
                else resultStr = "ERROR: 관리자에게 문의 바랍니다.";

                String message = "[" + storeName + "] " + resultStr;
                log.info("memberCode: {} | message: {}", memberCode, message);
                sseEmitter.send(SseEmitter.event().name("RESERVE_RESULT").data(message));

            } catch (Exception e) {
                log.error("SSE 알림 전송 실패");
                NotificationController.sseEmitters.remove(memberCode);
                throw new CustomException(ErrorCode.SOMETHING_WENT_WRONG);
            }
        }
    }


    /* [INTERNAL] 예약 실패 알림 - '나의 예약'에 조회되지 않고, 알림만 감 */
    // 왜냐하면, 실패한 예약 트랜잭션은 reservation 테이블에 등록되지 않기 때문
    public void notifyFailureToMember(Long memberCode, Long sid) { // sid: 알림을 받을 이용자가 예약에 실패한 팝업스토어 id
        // 추후에 에러핸들링 필요
        Store store = sRepo.findById(sid).get();

        if (NotificationController.sseEmitters.containsKey(memberCode)) {
            SseEmitter sseEmitter = NotificationController.sseEmitters.get(memberCode);
            try {
                String storeName = store.getStoreName(); // 이용자가 예약 관련 요청을 보낸 팝업스토어명
                String resultStr = "예약 정원이 마감되었습니다.";

                String message = "[" + storeName + "] " + resultStr;
                log.info("memberCode: {} | message: {}", memberCode, message);
                sseEmitter.send(SseEmitter.event().name("RESERVE_RESULT").data(message));

            } catch (Exception e) {
                log.error("SSE 알림 전송 실패");
                NotificationController.sseEmitters.remove(memberCode);
                throw new CustomException(ErrorCode.SOMETHING_WENT_WRONG);
            }
        }
    }


    /* [INTERNAL] 예약 요청 알림 - 이용자 to 운영자 */
    public void notifyReserveRequestToManager(Long memberCode, LocalDateTime dateTime, String requestType) {
        // view 구현 방향 상 당장은 reservation id가 여기에 필요하지 않을 듯
        // 허전하다 싶으면 rid 추가될 수도 있음
        if (NotificationController.sseEmitters.containsKey(memberCode)) {
            SseEmitter sseEmitter = NotificationController.sseEmitters.get(memberCode);
            try {
                String requestTypeStr = "";
                if (requestType.equals("CONFIRM_REQUEST")) requestTypeStr = "새로운 예약 신청이 있습니다.";
                else if (requestType.equals("CANCEL_REQUEST")) requestTypeStr = "새로운 예약 취소 요청이 있습니다.";
                else if (requestType.equals("AUTO_CONFIRMED")) requestTypeStr = "예약이 자동 승인되었습니다."; // V2는 자동 예약 확정
                else requestTypeStr = "ERROR: 관리자에게 문의 바랍니다.";

                String message = "[" + LocalDate.from(dateTime) + "] " + requestTypeStr;
                log.info("memberCode: {} | message: {}", memberCode, message);
                sseEmitter.send(SseEmitter.event().name("RESERVE_REQUEST").data(message));

            } catch (Exception e) {
                log.error("SSE 알림 전송 실패");
                NotificationController.sseEmitters.remove(memberCode);
                throw new CustomException(ErrorCode.SOMETHING_WENT_WRONG);
            }
        }
    }


    /* 특정 알림 삭제 - 서비스 정책 상 member가 읽었으면 해당 알림은 삭제 */
    public ResponseEntity<?> deleteNotification(Long nid, Long memberCode) {
        nRepo.deleteByNotificationIdAndMember_MemberCode(nid, memberCode);

        return ResponseEntity.ok().build();
    }
    
}
