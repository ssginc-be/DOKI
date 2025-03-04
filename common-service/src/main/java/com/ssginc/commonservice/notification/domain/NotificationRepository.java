package com.ssginc.commonservice.notification.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Queue-ri
 */

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByMember_MemberCode(Long memberCode);
    void deleteByNotificationIdAndMember_MemberCode(Long notificationId, Long memberCode);
}
