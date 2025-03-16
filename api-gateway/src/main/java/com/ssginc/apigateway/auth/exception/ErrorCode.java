package com.ssginc.apigateway.auth.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * @author Queue-ri
 */

@Getter
@AllArgsConstructor
public enum ErrorCode {
    /*
        4XX Error
    */
    // 400 BAD_REQUEST
    INVALID_FORMAT(HttpStatus.BAD_REQUEST, "DOKI-004", "요청 포맷이 잘못되었습니다."),

    // 401 UNAUTHORIZED
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "DOKI-003", "유효하지 않은 토큰입니다."), // 잘못된 토큰 or 만료된 토큰
    INVALID_CODE(HttpStatus.UNAUTHORIZED, "DOKI-012", "유효하지 않은 인증 코드입니다."), // 잘못된 인증 코드 or 만료된 인증 코드
    ROLE_NOT_FOUND(HttpStatus.UNAUTHORIZED, "DOKI-016", "권한 정보가 없습니다."), // 토큰이 필요한데 주어지지 않았을 때

    // 403 FORBIDDEN
    INVALID_ROLE(HttpStatus.FORBIDDEN, "DOKI-015", "접근 권한이 없습니다.");


    private final HttpStatus httpStatus;	// http 상태 코드
    private final String code;				// 시스템 내부 코드
    private final String message;			// 설명
}
