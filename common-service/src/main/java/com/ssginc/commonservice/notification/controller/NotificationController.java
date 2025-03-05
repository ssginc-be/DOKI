package com.ssginc.commonservice.notification.controller;

import com.ssginc.commonservice.notification.domain.Notification;
import com.ssginc.commonservice.notification.service.NotificationService;
import com.ssginc.commonservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Queue-ri
 */

@Slf4j
@RestController
@RequestMapping("/noti")
@RequiredArgsConstructor
public class NotificationController {
    /*
        SSE 연결, 알림 전체 조회, 특정 알림 삭제
    */
    private final JwtUtil jwtUtil;
    private final NotificationService notificationService;
    public static Map<Long, SseEmitter> sseEmitters = new ConcurrentHashMap<>();

    /* 로그인 유저 대상 SSE 연결 */
    @GetMapping("/subscribe")
    public SseEmitter subscribe(@CookieValue(value="accessToken", required=false) String accessToken) {
        // temp: API Gateway 임시 대체
        // 해당 API 호출 시점에서 role은 무조건 null이 아님
        Long code = Long.parseLong(jwtUtil.getClaims(accessToken).getSubject());

        return notificationService.subscribe(code); // return sseEmitter
    }

    /* 알림 전체 조회 */
    @GetMapping("/all")
    public List<Notification> getAll(@CookieValue(value="accessToken", required=false) String accessToken) {
        // temp: API Gateway 임시 대체
        // 해당 API 호출 시점에서 role은 무조건 null이 아님
        Long code = Long.parseLong(jwtUtil.getClaims(accessToken).getSubject());

        return notificationService.getAll(code);
    }

    /* 특정 알림 삭제 */
    @DeleteMapping
    public ResponseEntity<?> deleteNotification(
            @RequestParam("id") Long nid,
            @CookieValue(value="accessToken", required=false) String accessToken
    ) throws IOException {
        // temp: API Gateway 임시 대체
        // 해당 API 호출 시점에서 role은 무조건 null이 아님
        Long code = Long.parseLong(jwtUtil.getClaims(accessToken).getSubject());

        return notificationService.deleteNotification(nid, code);
    }
}
