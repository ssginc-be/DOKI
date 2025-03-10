package com.ssginc.commonservice.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * @author Queue-ri
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreSaveRequestDto {
    /*
        [관리자] 팝업스토어 등록 요청 dto
    */
    private String storeName;
    private List<Long> categoryList; // 카테고리 ID 리스트
    private String storeBranch; // 추후 Long branchId 로 리팩토링 필요
    private String storeAt;
    private String storeShortDesc;
    private String storeLongDesc;

    private LocalDate storeStartDate;
    private LocalDate storeEndDate;
    private String reserveMethod;

    private LocalTime storeStartTime;
    private LocalTime storeEndTime;
    private Integer reserveGap;
    private Integer capacity;

    // 대표 이미지 idx -> 대표 이미지로 썸네일 생성됨
    private Integer thumbIdx;

    // 이미지는 RestController에서 List<MultipartFile> 형식으로 받아서 처리함.
}
