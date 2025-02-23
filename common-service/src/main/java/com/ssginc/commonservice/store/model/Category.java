package com.ssginc.commonservice.store.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @author Queue-ri
 */

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    // 팝업스토어 - 카테고리 중계테이블 외래키
    @JsonManagedReference
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "category")
    private List<StoreCategory> storeCategoryList;

    @Column(nullable = false, length = 20)
    private String categoryName;

    @Column(length = 100)
    private String categoryDesc;
}
