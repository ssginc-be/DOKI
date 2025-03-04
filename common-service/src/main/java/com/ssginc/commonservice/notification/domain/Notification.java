package com.ssginc.commonservice.notification.domain;

import com.ssginc.commonservice.member.model.Member;
import jakarta.persistence.*;
import lombok.*;

/**
 * @author Queue-ri
 */

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    // member 외래키
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    Member member; // 해당 알림을 조회할 수 있는 이용자 or 운영자

    private NotificationType notiType;

    private String content;
}
