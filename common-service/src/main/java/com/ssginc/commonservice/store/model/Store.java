package com.ssginc.commonservice.store.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ssginc.commonservice.member.model.Member;
import com.ssginc.commonservice.reserve.model.Reservation;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storeId;

    @Column(nullable = false, length = 100)
    private String storeName;

    // 팝업스토어 - 카테고리 중계테이블 외래키
    @JsonManagedReference
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "store")
    private List<StoreCategory> storeCategoryList;

    @Column(nullable = false, length = 100)
    private String storeBranch; // 추후 Long branchId로 리팩토링 필요

    @Column(nullable = false, length = 200)
    private String storeAt;

    @Column(nullable = false, length = 300)
    private String storeShortDesc;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String storeLongDesc;

    @Column(nullable = false)
    private LocalDate storeStartDate;

    @Column(nullable = false)
    private LocalDate storeEndDate;

    @Column(nullable = false)
    private LocalTime storeStartTime;

    @Column(nullable = false)
    private LocalTime storeEndTime;

    @CreationTimestamp
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StoreStatus storeStatus;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StoreReserveMethod storeReserveMethod;

    @Column(nullable = false)
    private Integer storeReserveGap;

    @Column(nullable = false)
    private Integer storeCapacity;

    // 팝업스토어 이미지
    @JsonManagedReference
    @Column(nullable = false)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "store")
    private List<StoreImage> storeImageList;

    // 팝업스토어 계정
    @OneToOne(mappedBy = "store")
    private Member member;

    // 팝업스토어 예약 내역
    @JsonManagedReference
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "store")
    private List<Reservation> reservationList;


    // enum 객체
    public enum StoreStatus {
        ACTIVE, HIDDEN
    }
    
    public enum StoreReserveMethod {
        V1, V2 // V2는 Kafka 사용
    }
}
