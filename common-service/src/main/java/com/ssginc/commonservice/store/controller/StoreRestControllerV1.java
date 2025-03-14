package com.ssginc.commonservice.store.controller;

import com.ssginc.commonservice.store.dto.StoreSaveRequestDto;
import com.ssginc.commonservice.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

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
    /*
        [운영자] 예약 승인 / 거절 / 취소
    */
    /*
        [이용자] 특정 팝업스토어의 선택한 날짜에 대한 예약 엔트리 조회
    */
    /*
        [관리자] 팝업스토어 등록
    */
    
    private final StoreService storeService;

    
    /* 팝업스토어 목록 조회 */
    @GetMapping
    public ResponseEntity<?> getStoreList(@RequestParam(name="page", required=false) Integer pageIdx) {
        if (pageIdx == null) pageIdx = 0; // 루트 경로에서 호출 시 첫 페이지 조회
        return storeService.getStoreList(pageIdx);
    }
    
    /* 예약 승인 */
    @PutMapping("/reserve/confirm")
    public ResponseEntity<?> confirmReservation(@RequestParam(name="id") Long rid) {
        return storeService.confirmReservation(rid);
    }

    /* 예약 거절 */
    @PutMapping("/reserve/refuse")
    public ResponseEntity<?> refuseReservation(@RequestParam(name="id") Long rid) {
        return storeService.refuseReservation(rid);
    }

    /* 예약 취소 */
    @PutMapping("/reserve/cancel")
    public ResponseEntity<?> cancelReservation(@RequestParam(name="id") Long rid) {
        return storeService.cancelReservation(rid);
    }


    /* [이용자] 특정 팝업스토어의 선택한 날짜에 대한 예약 엔트리 조회 */
    @GetMapping("/entry")
    public ResponseEntity<?> getEntriesOfSpecificDate(
            @RequestParam(name="id") Long sid,
            @RequestParam(name="date") LocalDate date
    ) {
        return storeService.getEntriesOfSpecificDate(sid, date);
    }


    /* [관리자] 팝업스토어 등록 */
    @PostMapping(value="/registration", consumes={"multipart/form-data"})
    public ResponseEntity<?> registerStore(
            @RequestPart("json") StoreSaveRequestDto dto,
            @RequestPart("image") List<MultipartFile> mfiles
    ) {
        return storeService.registerStore(dto, mfiles);
    }
}
