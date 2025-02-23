package com.ssginc.commonservice.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Queue-ri
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {
    /*
        페이징 결과와 페이지 데이터를 response로 반환하고자 할 때 사용하는 객체
    */
    private T data;

    private Boolean first; // 첫 번째 페이지 여부
    private Boolean last; // 마지막 페이지 여부
    private Boolean empty; // data가 빈 리스트인지의 여부

    private Long totalElements; // 총 데이터 수
    private Integer totalPages; // 총 페이지 수, PageRequest.of 정의에 근거
    private Integer numberOfElements; // 현재 페이지의 데이터 수
    private Integer pageSize; // 페이징 단위
    private Integer pageNumber; // 현재 페이지 번호

}
