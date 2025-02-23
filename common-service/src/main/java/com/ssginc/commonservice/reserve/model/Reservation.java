package com.ssginc.commonservice.reserve.model;

import com.ssginc.commonservice.member.model.Member;
import com.ssginc.commonservice.store.model.Store;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
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
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    // 팝업스토어 외래키
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Store store;
    
    // 회원 외래키
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Member member;

    @Column(nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime reservedDateTime;

    @Column(nullable = false)
    private Integer headcount;

    @Column(nullable = false)
    @ColumnDefault("RESERVE_PENDING")
    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;

    @CreationTimestamp
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    public enum ReservationStatus {
        RESERVE_PENDING, CONFIRMED, CANCEL_PENDING, CANCELED
    };
}
