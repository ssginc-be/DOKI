package com.ssginc.commonservice.auth.controller;

import com.ssginc.commonservice.auth.dto.SignInRequestDto;
import com.ssginc.commonservice.auth.dto.SignUpRequestDto;
import com.ssginc.commonservice.auth.service.AuthService;
import com.ssginc.commonservice.util.JwtUtil;
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
        JWT - sign up, sign in, sign out, validate + parse, refresh
    */
    /*
        [이용자] 회원가입 휴대폰 인증코드 발송, 휴대폰 인증코드 확인
    */
    private final JwtUtil jwtUtil;
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
    public ResponseEntity<?> deleteToken(
            @RequestHeader(value="x-gateway-member-code", required=false) Long memberCode,
            @CookieValue(value="accessToken", required=false) String accessToken
    ) {
        //  temp: API Gateway 임시 대체
        Long code = jwtUtil.getMemberCode(accessToken);

        return authService.signOut(code);
    }

    @GetMapping("/info")
    public ResponseEntity<?> validateAndParse(@CookieValue("accessToken") String accessToken) {
        return authService.validateAndParse(accessToken);
    }

    @GetMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@CookieValue("refreshToken") String refreshToken) {
        return authService.refreshAccessToken(refreshToken);
    }


    /* [이용자] 회원가입 휴대폰 인증코드 발송 */
    @GetMapping("/phone/code")
    public ResponseEntity<?> sendPhoneValidationCode(@RequestParam("to") String receiverPhoneNum) {
        return authService.sendPhoneValidationCode(receiverPhoneNum);
    }

    /* [이용자] 회원가입 휴대폰 인증코드 확인 */
    @GetMapping("/phone/validation")
    public ResponseEntity<?> validatePhoneCode(@RequestParam("phone") String phoneNum, @RequestParam("code") String code) {
        return authService.validatePhoneCode(phoneNum, code);
    }
}
