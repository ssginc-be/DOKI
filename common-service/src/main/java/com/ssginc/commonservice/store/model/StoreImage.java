package com.ssginc.commonservice.store.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Queue-ri
 */

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoreImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storeImageId;

    // 팝업스토어 외래키
    @ManyToOne
    @JoinColumn
    private Store store;

    @Column(nullable = false, length = 50)
    private String storeImageTag;

    @Column(nullable = false, length = 500)
    private String storeImageLink;
}
