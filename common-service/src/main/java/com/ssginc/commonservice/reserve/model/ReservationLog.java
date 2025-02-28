package com.ssginc.commonservice.reserve.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
public class ReservationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationLogId;

    // 예약 내역 외래키
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Reservation reservation;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReserveMethod reserveMethod;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;

    @CreationTimestamp
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime reservationStatusTimestamp; // status 변경시마다 찍히는 타임스탬프
    
    // 예약대기 - 예약확정 - 취소대기 - 취소완료
    public enum ReservationStatus {
        RESERVE_PENDING, CONFIRMED, CANCEL_PENDING, CANCELED
    };

    public enum ReserveMethod {
        V1, V2 // V2는 Kafka 사용
    }
}
