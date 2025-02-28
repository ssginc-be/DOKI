package com.ssginc.commonservice.store.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ssginc.commonservice.member.model.Member;
import com.ssginc.commonservice.reserve.model.Reservation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
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
    private String storeBranch;

    @Column(nullable = false, length = 200)
    private String storeAt;

    @Column(nullable = false, length = 300)
    private String storeShortDesc;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String storeLongDesc;

    @Column(nullable = false)
    private LocalDate storeStart;

    @Column(nullable = false)
    private LocalDate storeEnd;

    @CreationTimestamp
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StoreStatus storeStatus;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StoreReserveMethod storeReserveMethod;

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
