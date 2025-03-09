package com.ssginc.commonservice.store.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

/**
 * @author Queue-ri
 */

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storeImageId;

    // 팝업스토어 외래키
    @JsonBackReference
    @ManyToOne
    @JoinColumn
    private Store store;

    @Column(nullable = false, length = 50)
    private String storeImageTag;

    @Column(nullable = false, length = 500)
    private String storeImageLink;
}
