package com.ssginc.commonservice.backdoor.service;

import com.ssginc.commonservice.store.document.StoreMetaDocument;
import com.ssginc.commonservice.store.document.StoreMetaDocumentRepository;
import com.ssginc.commonservice.store.dto.CategoryNoDescDto;
import com.ssginc.commonservice.store.dto.StoreMetaDto;
import com.ssginc.commonservice.store.model.Store;
import com.ssginc.commonservice.store.model.StoreCategory;
import com.ssginc.commonservice.store.model.StoreRepository;
import com.ssginc.commonservice.util.PageResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Queue-ri
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class PerfTestReadService {

    private final StoreRepository sRepo;
    private final StoreMetaDocumentRepository smdRepo; // Elasticsearch


    /* MySQL 팝업스토어 목록 조회 테스트 로직 */
    public Page getStoreList(Integer pageIdx) {
        // 페이징 크기
        final int FETCH_SIZE = 9;

        // PageRequest 객체 생성
        if (pageIdx > 0) pageIdx -= 1; // 사용자의 1페이지 == 서버의 0페이지
        PageRequest pageRequest = PageRequest.of(pageIdx, FETCH_SIZE, Sort.by("storeStartDate").descending());

        // 테이블에서 조회
        return sRepo.findAll(pageRequest);
    }


    /* Elasticsearch 팝업스토어 목록 조회 테스트 로직 */
    public Page getStoreListIndex(String keyword, Integer pageIdx) {
        // 페이징 크기
        final int FETCH_SIZE = 9;

        // PageRequest 객체 생성
        if (pageIdx > 0) pageIdx -= 1; // 사용자의 1페이지 == 서버의 0페이지
        PageRequest pageRequest = PageRequest.of(pageIdx, FETCH_SIZE, Sort.by("storeStartDate").descending());

        // 인덱스에서 조회 - 검색키워드 없음
        return smdRepo.findByStoreNameContainsIgnoreCase(keyword, pageRequest);
    }


    /* Redis Cache 팝업스토어 목록 조회 테스트 로직 */
    @Cacheable(value = "storeMetaListCache", cacheManager = "redisCacheManager")
    public Object getStoreListCache(String keyword, Integer pageIdx) {
        // 페이징 크기
        final int FETCH_SIZE = 9;

        // PageRequest 객체 생성
        if (pageIdx > 0) pageIdx -= 1; // 사용자의 1페이지 == 서버의 0페이지
        PageRequest pageRequest = PageRequest.of(pageIdx, FETCH_SIZE, Sort.by("storeStartDate").descending());

        // 인덱스에서 조회 - 검색키워드 없음
        return smdRepo.findByStoreNameContainsIgnoreCase(keyword, pageRequest);
    }
}
