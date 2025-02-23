package com.ssginc.commonservice.util;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * @author Queue-ri
 */

@Component
public class CookieUtil {

    public ResponseCookie generateAccessTokenCookie(String accessToken, Long maxAge) {
        return ResponseCookie.from("accessToken", accessToken)
                .path("/")
                .sameSite("Strict")
                .httpOnly(true)
                .secure(false) // 프로덕션(https)에선 반드시 true로 설정하기
                .maxAge(maxAge)
                .build();
    }

    public ResponseCookie generateRefreshTokenCookie(String refreshToken, Long maxAge) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .path("/")
                .sameSite("Strict")
                .httpOnly(true)
                .secure(false) // 프로덕션(https)에선 반드시 true로 설정하기
                .maxAge(maxAge)
                .build();
    }

    public ResponseCookie expireAccessTokenCookie() {
        return ResponseCookie.from("accessToken", "")
                .path("/")
                .sameSite("Strict")
                .httpOnly(true)
                .secure(false) // 프로덕션(https)에선 반드시 true로 설정하기
                .maxAge(0)
                .build();
    }

    public ResponseCookie expireRefreshTokenCookie() {
        return ResponseCookie.from("refreshToken", "")
                .path("/")
                .sameSite("Strict")
                .httpOnly(true)
                .secure(false) // 프로덕션(https)에선 반드시 true로 설정하기
                .maxAge(0)
                .build();
    }
}
