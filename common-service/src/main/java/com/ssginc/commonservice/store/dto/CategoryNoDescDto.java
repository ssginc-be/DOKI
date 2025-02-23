package com.ssginc.commonservice.store.dto;

import com.ssginc.commonservice.store.model.StoreCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Queue-ri
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryNoDescDto {
    /*
        store dto 내부에 들어가는 category dto - desc 필드 없음
    */
    private Long categoryId;

    private String categoryName;

    // store 조회 -> 중계 테이블의 category 접근 -> dto 변환하고자 할 때 사용되는 생성자
    public CategoryNoDescDto(StoreCategory storeCategory) {
        categoryId = storeCategory.getCategory().getCategoryId();
        categoryName = storeCategory.getCategory().getCategoryName();
    }
}
