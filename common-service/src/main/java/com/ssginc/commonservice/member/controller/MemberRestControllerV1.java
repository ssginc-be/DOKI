package com.ssginc.commonservice.member.controller;

import com.ssginc.commonservice.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Queue-ri
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/member")
public class MemberRestControllerV1 {

    private final MemberService memberService;

    @GetMapping("/reserve/cancel")
    public ResponseEntity<?> requestCancel(@RequestParam(name="id") Long rid) {
        return memberService.requestReservationCancel(rid);
    }
}
