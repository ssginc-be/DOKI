package com.ssginc.commonservice.store.service;

import com.ssginc.commonservice.store.model.Store;
import com.ssginc.commonservice.store.model.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
        Page<Store> storeList = sRepo.findAll(pageRequest);

        System.out.println(storeList);

        return ResponseEntity.ok().body(storeList);
    }
}
