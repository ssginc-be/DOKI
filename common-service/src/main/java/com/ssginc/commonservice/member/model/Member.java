package com.ssginc.commonservice.member.model;

import com.ssginc.commonservice.reserve.model.Reservation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Queue-ri
 */

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_code")
    private Long memberCode; // 조회 최적화용 PK

    @Column(name = "member_id", nullable = false, length = 50)
    private String memberId; // 로그인용 ID

    @Column(name = "member_pw", nullable = false, length = 500)
    private String memberPw;

    @Column(name = "member_name", nullable = false, length = 50)
    private String memberName;

    @Column(name = "member_phone", nullable = false)
    private String memberPhone;

    @Column(name = "member_birth", nullable = false)
    private LocalDate memberBirth;

    @Column(name = "member_gender", nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberGender memberGender;

    @Column(name = "member_role", nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberRole memberRole;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "last_modified_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime lastModifiedAt;

    // 팝업스토어 예약 내역
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "member")
    private List<Reservation> reservationList;

    // Enum 객체
    public enum MemberGender {
        MALE, FEMALE, NOT_SPECIFIED
    };

    public enum MemberRole {
        MEMBER, MANAGER, ADMIN
    };
}
