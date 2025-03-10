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
    /*
        4XX Error
    */
    // 400 BAD_REQUEST
    INVALID_FORMAT(HttpStatus.BAD_REQUEST, "DOKI-004", "요청 포맷이 잘못되었습니다."),

    // 401 UNAUTHORIZED
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "DOKI-003", "유효하지 않은 토큰입니다."), // 잘못된 토큰 or 만료된 토큰
    INVALID_CODE(HttpStatus.UNAUTHORIZED, "DOKI-012", "유효하지 않은 인증 코드입니다."), // 잘못된 인증 코드 or 만료된 인증 코드

    // 403 FORBIDDEN
    RESERVATION_ALREADY_END(HttpStatus.FORBIDDEN, "DOKI-006", "예약이 마감되었습니다."),
    RESERVATION_EXISTS(HttpStatus.FORBIDDEN, "DOKI-007", "해당 일시의 예약 이력이 있습니다."),
    EXCEED_RESERVATION_CAPACITY(HttpStatus.FORBIDDEN, "DOKI-009", "예약 정원을 초과하여 승인할 수 없습니다."),

    // 404 NOT_FOUND
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "DOKI-001", "사용자를 찾을 수 없습니다."),
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "DOKI-005", "팝업스토어를 찾을 수 없습니다."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "DOKI-008", "예약 이력을 찾을 수 없습니다."),

    // 409 CONFLICT
    HAS_EMAIL(HttpStatus.CONFLICT, "DOKI-002", "존재하는 이메일입니다."),


    /*
        5XX Error
    */
    // 502 BAD_GATEWAY
    CANNOT_UPLOAD_IMAGE(HttpStatus.BAD_GATEWAY, "DOKI-010", "이미지 업로드에 실패했습니다."),
    CANNOT_SEND_MESSAGE(HttpStatus.BAD_GATEWAY, "DOKI-011", "문자 전송에 실패했습니다."),

    // 500 INTERNAL_SERVER_ERROR
    SOMETHING_WENT_WRONG(HttpStatus.INTERNAL_SERVER_ERROR, "DOKI-099", "알 수 없는 오류가 발생했습니다.");


    private final HttpStatus httpStatus;	// http 상태 코드
    private final String code;				// 시스템 내부 코드
    private final String message;			// 설명
}
