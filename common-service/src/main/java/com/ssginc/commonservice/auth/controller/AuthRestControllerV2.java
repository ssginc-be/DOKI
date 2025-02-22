package com.ssginc.commonservice.auth.controller;

import com.ssginc.commonservice.auth.dto.SignInRequestDto;
import com.ssginc.commonservice.auth.dto.SignUpRequestDto;
import com.ssginc.commonservice.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author Queue-ri
 */

@RequiredArgsConstructor
@RestController
@RequestMapping("/v2/auth")
public class AuthRestControllerV2 {
    /*
        sign up, sign in, sign out, validate + parse, refresh
    */
    private final AuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequestDto dto) {
        return authService.signUp(dto);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody SignInRequestDto dto) {
        return authService.signInV2(dto);
    }

    @DeleteMapping("/sign-out")
    public ResponseEntity<?> deleteToken(@RequestHeader("x-gateway-member-code") Long memberCode) {
        return authService.signOut(memberCode);
    }

    @GetMapping("/info")
    public ResponseEntity<?> validateAndParse(@CookieValue("accessToken") String accessToken) {
        return authService.validateAndParse(accessToken);
    }

    @GetMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@CookieValue("refreshToken") String refreshToken) {
        return authService.refreshAccessToken(refreshToken);
    }
}
