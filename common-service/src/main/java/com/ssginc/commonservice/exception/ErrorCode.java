package com.ssginc.commonservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * @author Queue-ri
 */

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "DOKI-001", "사용자를 찾을 수 없습니다."),
    HAS_EMAIL(HttpStatus.CONFLICT, "DOKI-002", "존재하는 이메일입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "DOKI-003", "잘못된 토큰입니다."), // 만료된 토큰도 이에 해당
    INVALID_FORMAT(HttpStatus.BAD_REQUEST, "DOKI-004", "요청 포맷이 잘못되었습니다.");

    private final HttpStatus httpStatus;	// http 상태 코드
    private final String code;				// 시스템 내부 코드
    private final String message;			// 설명
}
