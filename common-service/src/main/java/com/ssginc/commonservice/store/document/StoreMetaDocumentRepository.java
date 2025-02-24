package com.ssginc.commonservice.store.document;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Queue-ri
 */

public interface StoreMetaDocumentRepository extends ElasticsearchRepository<StoreMetaDocument, Long> {
    // 전체 인덱스 조회
    List<StoreMetaDocument> findAll();

    // 키워드 검색을 통한 팝업스토어 목록 조회
    Page<StoreMetaDocument> findByStoreNameContainsIgnoreCase(String storeName, PageRequest pageRequest);

}
