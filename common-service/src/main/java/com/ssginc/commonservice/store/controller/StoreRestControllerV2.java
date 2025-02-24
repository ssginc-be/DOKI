package com.ssginc.commonservice.store.controller;

import com.ssginc.commonservice.store.service.StoreIndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Queue-ri
 */

@RequiredArgsConstructor
@RestController
@RequestMapping("/v2/store")
public class StoreRestControllerV2 {
    /*
        Elasticsearch 인덱스를 통한 팝업스토어 목록 조회
    */
    private final StoreIndexService storeIndexService;

    @GetMapping
    public ResponseEntity<?> getStoreListByKeyword(@RequestParam(name="page", required=false) Integer pageIdx, String keyword) {
        if (pageIdx == null) pageIdx = 0; // 루트 경로에서 호출 시 첫 페이지 조회
        return storeIndexService.getStoreListByKeyword(pageIdx, keyword);
    }
}
