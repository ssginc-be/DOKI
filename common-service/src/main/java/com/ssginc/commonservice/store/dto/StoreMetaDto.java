package com.ssginc.commonservice.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Queue-ri
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreMetaDto {
    /*
        목록 조회용 store dto - 메타데이터만 포함됨
    */
    private Long storeId;

    private List<CategoryNoDescDto> categoryList; // 카테고리 id와 name만 포함

    private String storeName;

    private String storeShortDesc;

    private LocalDate storeStart;

    private LocalDate storeEnd;

    private String storeMainThumbnail; // 목록에서 보여질 대표 이미지 1장
}
