package com.ssginc.commonservice.store.controller;

import com.ssginc.commonservice.exception.CustomException;
import com.ssginc.commonservice.exception.ErrorCode;
import com.ssginc.commonservice.member.service.MemberService;
import com.ssginc.commonservice.reserve.dto.ReserveRequestDto;
import com.ssginc.commonservice.reserve.service.ReserveService;
import com.ssginc.commonservice.store.document.StoreMetaDocument;
import com.ssginc.commonservice.store.model.Store;
import com.ssginc.commonservice.store.model.StoreCategory;
import com.ssginc.commonservice.store.model.StoreImage;
import com.ssginc.commonservice.store.service.StoreIndexService;
import com.ssginc.commonservice.store.service.StoreService;
import com.ssginc.commonservice.util.JwtUtil;
import com.ssginc.commonservice.util.PageResponseDto;
import com.ssginc.commonservice.util.RequestInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Queue-ri
 */

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping // root 경로가 곧 팝업스토어 목록 조회 페이지
public class StoreController {
    /*
        store list(main page), store search result, store info,
        store reservation, store reservation log page
    */
    private final JwtUtil jwtUtil;
    private final StoreService storeService;
    private final ReserveService reserveService;
    private final StoreIndexService storeIndexService; // Elasticsearch
    private final MemberService memberService;

    /* 메인 페이지 */
    @GetMapping
    public String viewStoreList(
            @RequestHeader(value="x-gateway-member-role", required=false) String memberRole,
            @RequestHeader(value="x-gateway-member-code", required=false) Long memberCode,
            @RequestHeader(value="x-gateway-request-uuid", required=false) String requestUuid,
            @CookieValue(value="accessToken", required=false) String accessToken,
            @RequestParam(value="page", required=false) Integer pageIdx,
            Model model
    ) {
        RequestInfoDto requestInfo = mockGateway(accessToken);
        model.addAttribute("memberRole", requestInfo.getMemberRole()); // null 또는 MEMBER
        String role = requestInfo.getMemberRole();
        model.addAttribute("memberCode", requestInfo.getMemberCode());
        model.addAttribute("requestUuid", requestInfo.getRequestUuid());

        // 1. 비회원이거나 로그인한 이용자 (null 체크가 조건식 앞에 있어야 함)
        if (role == null || role.equals("MEMBER")) {
            if (pageIdx == null) pageIdx = 0; // 루트 경로에서 호출 시 첫 페이지 조회
            PageResponseDto page = storeIndexService.getStoreListInternal("", pageIdx); // v2 팝업스토어 조회

            model.addAttribute("page", page);
            List<StoreMetaDocument> storeList = (List<StoreMetaDocument>) page.getData(); // downcast
            model.addAttribute("storeList", storeList);
            model.addAttribute("formatter", DateTimeFormatter.ofPattern("MM.dd(E)"));

            return "index"; // 팝업스토어 목록 페이지로 이동
        }

        // 2. 운영자 계정으로 접속 시
        else if (role.equals("MANAGER")) {
            model.addAttribute("menuIdx", 0);
            Store store = memberService.getMemberInfo(requestInfo.getMemberCode()).getStore();
            model.addAttribute("store", store);
            model.addAttribute("storeName", store.getStoreName());

            // 팝업스토어 카테고리 리스트 가져오기
            List<String> categoryNameList = new ArrayList<>();
            for (StoreCategory sc : store.getStoreCategoryList()) {
                categoryNameList.add(sc.getCategory().getCategoryName());
            }

            // 팝업스토어 썸네일 이미지 가져오기
            StoreImage thumbnail = store.getStoreImageList().stream()
                    .filter(img -> "SUB_THUMBNAIL".equals(img.getStoreImageTag()))
                    .findFirst()
                    .orElseThrow(() -> new CustomException(ErrorCode.THUMBNAIL_NOT_FOUND));

            // 팝업스토어 상세 이미지 가져오기
            List<StoreImage> contentDetailList = store.getStoreImageList().stream()
                    .filter(img -> "CONTENT_DETAIL".equals(img.getStoreImageTag()))
                    .toList();

            model.addAttribute("categoryNameList", categoryNameList);
            model.addAttribute("thumbnail", thumbnail);
            model.addAttribute("contentDetailList", contentDetailList);

            return "manager/manager_store_info"; // 운영자 페이지의 첫 메뉴로 이동
        }

        // 3. 관리자 계정으로 접속 시
        else if (role.equals("ADMIN")) {
            model.addAttribute("menuIdx", 0);
            return "admin/admin_store_registration"; // 관리자 페이지의 첫 메뉴로 이동
        }

        // 4. 그 외 이상한 경우
        else {
            log.error("알 수 없는 오류");
            throw new CustomException(ErrorCode.SOMETHING_WENT_WRONG);
        }
    }

    /* 팝업스토어 검색 결과 페이지 */
    @GetMapping("/search")
    public String viewSearchResultPage(
            @RequestHeader(value="x-gateway-member-role", required=false) String memberRole,
            @CookieValue(value="accessToken", required=false) String accessToken,
            @RequestParam("q") String keyword,
            @RequestParam(value="page", required = false) Integer pageIdx,
            Model model
    ) {
        RequestInfoDto requestInfo = mockGateway(accessToken);
        model.addAttribute("memberRole", requestInfo.getMemberRole()); // null 또는 MEMBER
        model.addAttribute("memberCode", requestInfo.getMemberCode());
        model.addAttribute("requestUuid", requestInfo.getRequestUuid());

        model.addAttribute("keyword", keyword);

        if (pageIdx == null) pageIdx = 0; // 별도의 pageIdx가 주어지지 않으면 기본값으로 첫 페이지 조회
        PageResponseDto page = storeIndexService.getStoreListInternal(keyword, pageIdx); // v2 팝업스토어 조회

        model.addAttribute("page", page);
        List<StoreMetaDocument> storeList = (List<StoreMetaDocument>) page.getData(); // downcast
        model.addAttribute("storeList", storeList);
        model.addAttribute("formatter", DateTimeFormatter.ofPattern("MM.dd(E)"));

        return "store/store_search_result";
    }

    /* 팝업스토어 상세 조회 페이지 */
    @GetMapping("/store")
    public String viewStoreInfo(
            @RequestHeader(value="x-gateway-member-role", required=false) String memberRole,
            @CookieValue(value="accessToken", required=false) String accessToken,
            @RequestParam("id") Long storeId,
            Model model
    ) {
        RequestInfoDto requestInfo = mockGateway(accessToken);
        model.addAttribute("memberRole", requestInfo.getMemberRole()); // null 또는 MEMBER
        model.addAttribute("memberCode", requestInfo.getMemberCode());
        model.addAttribute("requestUuid", requestInfo.getRequestUuid());

        boolean isPreview = false; // 미리보기 모드 플래그
        if (accessToken != null) isPreview = requestInfo.getMemberRole().equals("MANAGER");
        model.addAttribute("isPreview", isPreview);

        Store store = storeService.getStoreInfo(storeId);
        model.addAttribute("store", store);
        model.addAttribute("formatter", DateTimeFormatter.ofPattern("MM.dd(E)"));

        // 팝업스토어 상세 이미지 가져오기
        List<StoreImage> contentDetailList = store.getStoreImageList().stream()
                .filter(img -> "CONTENT_DETAIL".equals(img.getStoreImageTag()))
                .toList();

        model.addAttribute("contentDetailList", contentDetailList);

        return "store/store_info";
    }

    /* [운영자] 예약 승인 / 예약 거절 페이지 */
    @GetMapping("/store/reserve")
    public String viewStoreReservationPage(
            @RequestHeader(value="x-gateway-member-role", required=false) String memberRole,
            @CookieValue(value="accessToken", required=false) String accessToken,
            Model model
    ) {
        // 운영자 페이지이므로 role은 무조건 MANAGER여야 함
        RequestInfoDto requestInfo = mockGateway(accessToken);
        model.addAttribute("memberRole", requestInfo.getMemberRole()); // null 또는 MEMBER
        model.addAttribute("memberCode", requestInfo.getMemberCode());
        model.addAttribute("requestUuid", requestInfo.getRequestUuid());

        Store store = memberService.getMemberInfo(requestInfo.getMemberCode()).getStore();
        model.addAttribute("storeName", store.getStoreName());

        model.addAttribute("menuIdx", 3);

        return "manager/manager_store_reservation";
    }

    /* [운영자] 예약 관리 이력 페이지 */
    @GetMapping("/store/reserve/log")
    public String viewStoreReservationLogPage(
            @RequestHeader(value="x-gateway-member-role", required=false) String memberRole,
            @CookieValue(value="accessToken", required=false) String accessToken,
            Model model
    ) {
        // 운영자 페이지이므로 role은 무조건 MANAGER여야 함
        RequestInfoDto requestInfo = mockGateway(accessToken);
        model.addAttribute("memberRole", requestInfo.getMemberRole()); // null 또는 MEMBER
        model.addAttribute("memberCode", requestInfo.getMemberCode());
        model.addAttribute("requestUuid", requestInfo.getRequestUuid());

        Store store = memberService.getMemberInfo(requestInfo.getMemberCode()).getStore();
        model.addAttribute("storeName", store.getStoreName());
        // 해당 페이지는 테이블에서 보여줄 memberName도 필요
        model.addAttribute("memberName", store.getMember().getMemberName());

        model.addAttribute("menuIdx", 5);

        return "manager/manager_store_reservation_log";
    }


    private RequestInfoDto mockGateway(String accessToken) {
        String role = null;
        Long code = null;
        String uuid = UUID.randomUUID().toString();

        if (accessToken != null) {
            role = jwtUtil.getClaims(accessToken).get("role").toString();
            code = Long.parseLong(jwtUtil.getClaims(accessToken).getSubject());
        }

        log.info("requested role: {}", role);
        log.info("requested code: {}", code);
        log.info("requested uuid: {}", uuid);

        return RequestInfoDto.builder().memberCode(code).memberRole(role).requestUuid(uuid).build();
    }

}
