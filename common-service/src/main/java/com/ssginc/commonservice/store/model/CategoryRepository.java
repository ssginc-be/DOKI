package com.ssginc.commonservice.store.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Queue-ri
 */

public interface CategoryRepository extends JpaRepository<Category, Long> {

    /* Category ID 배열을 통해 값이 일치하는 모든 Category 엔티티 조회 */
    @Query(value = "SELECT c FROM Category c WHERE c.categoryId IN :categoryIdList")
    List<Category> findAllByCategoryIdList(@Param("categoryIdList") List<Long> categoryIdList);

}
