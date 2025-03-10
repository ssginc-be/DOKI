package com.ssginc.commonservice.member.controller;

import com.ssginc.commonservice.member.service.MemberService;
import com.ssginc.commonservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * @author Queue-ri
 */

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/member")
public class MemberRestControllerV1 {
    /*
        '나의 예약' 페이지 예약 내역 조회,
        '나의 예약' 페이지 예약 취소 요청
    */

    private final MemberService memberService;
    private final JwtUtil jwtUtil;

    @GetMapping("/reserve")
    public ResponseEntity<?> getMyReservationList(@CookieValue(value="accessToken", required=false) String accessToken) {
        // temp: API Gateway 임시 대체
        // role은 반드시 MEMBER여야 함
        String role = jwtUtil.getClaims(accessToken).get("role").toString();
        log.info("requested role: {}", role);

        Long code = Long.parseLong(jwtUtil.getClaims(accessToken).getSubject());
        log.info("requested code: {}", code);

        return memberService.getMyReservationList(code);
    }

    @GetMapping("/reserve/cancel")
    public ResponseEntity<?> requestCancel(@RequestParam(name="id") Long rid) {
        return memberService.requestReservationCancel(rid);
    }
}
