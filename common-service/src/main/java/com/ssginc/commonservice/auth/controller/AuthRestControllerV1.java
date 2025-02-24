package com.ssginc.commonservice.auth.controller;

import com.ssginc.commonservice.auth.dto.SignInRequestDto;
import com.ssginc.commonservice.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author Queue-ri
 */

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/auth")
public class AuthRestControllerV1 {
    /*
        성능 테스트용 - sign in
    */
    private final AuthService authService;

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody SignInRequestDto dto) {
        //return authService.signInV1(dto);
        return null;
    }
}
