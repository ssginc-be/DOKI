package com.ssginc.commonservice.member.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * @author Queue-ri
 */

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @ManyToOne
    @JoinColumn
    private Member reportedMemberCode;

    @ManyToOne
    @JoinColumn
    private Member reporterMemberCode;

    @Column(nullable = false, length = 300)
    private String reason;

    @CreationTimestamp
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime reportedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportResult reportResult;

    public enum ReportResult {
        PENDING, TEMP_7, TEMP_30, PERMANENT
    }
}
