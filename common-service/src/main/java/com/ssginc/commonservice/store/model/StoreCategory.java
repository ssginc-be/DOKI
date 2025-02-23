package com.ssginc.commonservice.store.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
public class StoreCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storeCategoryId;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Store store;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Category category;
}
