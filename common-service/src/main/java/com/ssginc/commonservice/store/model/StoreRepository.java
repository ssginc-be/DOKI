package com.ssginc.commonservice.store.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Queue-ri
 */

public interface StoreRepository extends JpaRepository<Store, Long> {
    Page<Store> findAllByStoreCategoryList_Category_CategoryId(Long categoryId, PageRequest pageRequest);
}
