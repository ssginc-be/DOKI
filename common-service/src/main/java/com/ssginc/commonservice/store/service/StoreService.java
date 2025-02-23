package com.ssginc.commonservice.store.service;

import com.ssginc.commonservice.store.dto.CategoryNoDescDto;
import com.ssginc.commonservice.store.dto.StoreMetaDto;
import com.ssginc.commonservice.store.model.Store;
import com.ssginc.commonservice.store.model.StoreCategory;
import com.ssginc.commonservice.store.model.StoreRepository;
import com.ssginc.commonservice.util.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Queue-ri
 */

@Slf4j
@RequiredArgsConstructor
@Service
public class StoreService {
    /*
        팝업스토어 목록 조회
    */
    private final StoreRepository sRepo;


    /* 팝업스토어 목록 조회 */
    public ResponseEntity<?> getStoreList(Integer pageIdx) {
        // 페이징 크기
        final int FETCH_SIZE = 9;

        // PageRequest 객체 생성
        PageRequest pageRequest = PageRequest.of(pageIdx, FETCH_SIZE, Sort.by("storeStart").descending());

        // 테이블에서 조회
        Page<Store> storePage = sRepo.findAll(pageRequest);

        // store -> dto 및 category -> dto 변환
        List<StoreMetaDto> data = new ArrayList<>();
        for (Store store : storePage.getContent()) {
            // 중계테이블 스키마 -> desc 없는 category dto로 변환
            List<StoreCategory> scList = store.getStoreCategoryList();
            List<CategoryNoDescDto> dtoList = scList.stream().map(CategoryNoDescDto::new).toList();

            data.add(
                    StoreMetaDto.builder()
                            .storeId(store.getStoreId())
                            .categoryList(dtoList)
                            .storeName(store.getStoreName())
                            .storeShortDesc(store.getStoreShortDesc())
                            .storeStart(store.getStoreStart())
                            .storeEnd(store.getStoreEnd())
                            .storeMainThumbnail(store.getStoreImageList().stream()
                                    .filter(img -> img.getStoreImageTag().equals("MAIN_THUMBNAIL")).toList().get(0).getStoreImageLink())
                            .build()
            );
        }

        // 반환할 page 객체 작성
        PageResponse<?> page = PageResponse.builder()
                .data(data)
                .first(storePage.isFirst())
                .last(storePage.isLast())
                .empty(storePage.isEmpty())
                .totalElements(storePage.getTotalElements())
                .totalPages(storePage.getTotalPages())
                .numberOfElements(storePage.getNumberOfElements())
                .pageSize(storePage.getSize())
                .pageNumber(storePage.getNumber())
                .build();

        return ResponseEntity.ok().body(page);
    }
}
