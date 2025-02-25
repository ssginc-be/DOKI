package com.ssginc.commonservice.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Queue-ri
 */

@RequiredArgsConstructor
@Controller
@RequestMapping("/auth") // root 경로가 곧 팝업스토어 목록 조회 페이지
public class AuthController {
    /*
        sign up (sign in은 오버레이 형식으로 팝업하므로 별도의 페이지 없음)
    */
    @GetMapping("/sign-up")
    public String signUp() {
        return "auth/sign-up";
    }

}
