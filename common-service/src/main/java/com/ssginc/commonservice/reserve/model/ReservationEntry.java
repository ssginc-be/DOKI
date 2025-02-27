package com.ssginc.commonservice.reserve.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.ssginc.commonservice.store.model.Store;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @author Queue-ri
 */

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationEntryId;

    // 팝업스토어 외래키
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Store store;

    @Column(nullable = false)
    private LocalDate entryDate; // 예약 엔트리 날짜

    @Column(nullable = false)
    private LocalTime entryTime; // 예약 엔트리 시간

    @Column(nullable = false)
    private Integer capacity; // 정원

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer reservedCount; // 예약자 수

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EntryStatus entryStatus;
    
    // 예약대기 - 예약확정 - 취소대기 - 취소완료
    public enum EntryStatus {
        OPEN, CLOSED
    };
}
