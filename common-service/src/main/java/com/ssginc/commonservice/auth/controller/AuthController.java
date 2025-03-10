package com.ssginc.commonservice.auth.controller;

import com.ssginc.commonservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Queue-ri
 */

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/auth")
public class AuthController {
    /*
        view sign-up page - 회원가입 페이지 조회
    */
    private final JwtUtil jwtUtil;

    @GetMapping("/sign-up")
    public String viewSignUpPage() {
        return "auth/sign_up";
    }
}
