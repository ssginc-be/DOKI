package com.ssginc.commonservice.notification.domain;

/**
 * @author Queue-ri
 */

public enum NotificationType {
    RESERVE_REQUEST, // 이용자 -> 운영자로 가는 예약 관련 요청
    RESERVE_RESULT // 운영자 -> 이용자로 가는 예약 요청 결과 응답
}
