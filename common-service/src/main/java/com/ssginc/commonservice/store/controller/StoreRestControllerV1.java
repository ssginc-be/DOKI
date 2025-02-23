package com.ssginc.commonservice.store.controller;

import com.ssginc.commonservice.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author Queue-ri
 */

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/store")
public class StoreRestControllerV1 {
    /*
        성능 테스트용 - 팝업스토어 목록 조회
    */
    private final StoreService storeService;

    @GetMapping
    public ResponseEntity<?> getStoreList(@RequestParam(name="page", required=false) Integer pageIdx) {
        if (pageIdx == null) pageIdx = 0; // 루트 경로에서 호출 시 첫 페이지 조회
        return storeService.getStoreList(pageIdx);
    }
}
