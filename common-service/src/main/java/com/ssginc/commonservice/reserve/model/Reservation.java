package com.ssginc.commonservice.reserve.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ssginc.commonservice.member.model.Member;
import com.ssginc.commonservice.store.model.Store;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Queue-ri
 */

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    // 팝업스토어 외래키
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Store store;
    
    // 회원 외래키
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Member member;

    // 예약 엔트리 외래키
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ReservationEntry reservationEntry;

    @Column(nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime reservedDateTime; // 예약 일시 -> 타임스탬프 아님

    @Column(nullable = false)
    private Integer headcount; // 예약 인원

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;

    @CreationTimestamp
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt; // 내역 생성일시 타임스탬프
    
    // 예약 로그
    @JsonManagedReference
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "reservation")
    private List<ReservationLog> reservationLogList;

    // 예약대기 - 예약확정 - 취소대기 - 취소완료
    public enum ReservationStatus {
        RESERVE_PENDING, CONFIRMED, REFUSED, CANCEL_PENDING, CANCELED
    };
}
