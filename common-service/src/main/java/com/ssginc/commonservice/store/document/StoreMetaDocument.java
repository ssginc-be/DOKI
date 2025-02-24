package com.ssginc.commonservice.store.document;

import com.ssginc.commonservice.store.dto.CategoryNoDescDto;
import com.ssginc.commonservice.store.model.Store;
import com.ssginc.commonservice.store.model.StoreCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Queue-ri
 */

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "store_meta")
public class StoreMetaDocument {
    /*
        목록 조회용 store_meta document - 메타데이터만 포함됨
    */
    @Id
    private Long storeId;

    private List<CategoryNoDescDto> categoryList; // 카테고리 id와 name만 포함

    private String storeName;

    private String storeShortDesc;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd")
    private LocalDate storeStart;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd")
    private LocalDate storeEnd;

    private String storeMainThumbnail; // 목록에서 보여질 대표 이미지 1장

    public static StoreMetaDocument from(Store store) {
        List<StoreCategory> scList = store.getStoreCategoryList();
        List<CategoryNoDescDto> dtoList = scList.stream().map(CategoryNoDescDto::new).toList();

        return StoreMetaDocument.builder()
                .storeId(store.getStoreId())
                .categoryList(dtoList)
                .storeName(store.getStoreName())
                .storeShortDesc(store.getStoreShortDesc())
                .storeStart(store.getStoreStart())
                .storeEnd(store.getStoreEnd())
                .storeMainThumbnail(store.getStoreImageList().stream()
                        .filter(img -> img.getStoreImageTag().equals("MAIN_THUMBNAIL")).toList().get(0).getStoreImageLink())
                .build();
    }
}
