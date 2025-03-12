package com.ssginc.commonservice.store.controller;

import com.ssginc.commonservice.exception.CustomException;
import com.ssginc.commonservice.exception.ErrorCode;
import com.ssginc.commonservice.member.service.MemberService;
import com.ssginc.commonservice.reserve.model.Reservation;
import com.ssginc.commonservice.reserve.service.ReserveService;
import com.ssginc.commonservice.store.document.StoreMetaDocument;
import com.ssginc.commonservice.store.model.Store;
import com.ssginc.commonservice.store.model.StoreCategory;
import com.ssginc.commonservice.store.model.StoreImage;
import com.ssginc.commonservice.store.service.StoreIndexService;
import com.ssginc.commonservice.store.service.StoreService;
import com.ssginc.commonservice.util.JwtUtil;
import com.ssginc.commonservice.util.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Queue-ri
 */

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/") // root 경로가 곧 팝업스토어 목록 조회 페이지
public class StoreController {
    /*
        store list, store info, store reservation, store reservation log page
    */
    private final JwtUtil jwtUtil;
    private final StoreService storeService;
    private final ReserveService reserveService;
    private final StoreIndexService storeIndexService; // Elasticsearch
    private final MemberService memberService;

    @GetMapping
    public String viewStoreList(
            @RequestHeader(value="x-gateway-member-role", required=false) String memberRole,
            @CookieValue(value="accessToken", required=false) String accessToken,
            @RequestParam(value="page", required = false) Integer pageIdx,
            Model model
    ) {
        // temp: API Gateway 임시 대체
        String role = null;
        if (accessToken != null) role = jwtUtil.getClaims(accessToken).get("role").toString();
        log.info("requested role: {}", role);
        model.addAttribute("memberRole", role);

        Long code = null;
        if (accessToken != null) code = Long.parseLong(jwtUtil.getClaims(accessToken).getSubject());
        log.info("requested code: {}", code);
        model.addAttribute("memberCode", code);

        if (role == null || role.equals("MEMBER")) { // 비회원이거나 로그인한 이용자 (null 체크가 맨 위에 있어야 함)
            // role.equals("MEMBER")
            if (pageIdx == null) pageIdx = 0; // 루트 경로에서 호출 시 첫 페이지 조회
            PageResponse page = storeIndexService.getStoreListInternal(pageIdx); // v2 팝업스토어 조회

            model.addAttribute("page", page);
            List<StoreMetaDocument> storeList = (List<StoreMetaDocument>) page.getData(); // downcast
            model.addAttribute("storeList", storeList);
            model.addAttribute("formatter", DateTimeFormatter.ofPattern("MM.dd(E)"));

            return "index"; // 팝업스토어 목록 페이지로 이동
        }
        else if (role.equals("MANAGER")) {
            model.addAttribute("menuIdx", 0);
            Store store = memberService.getMemberInfo(code).getStore();
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
        else if (role.equals("ADMIN")) {
            model.addAttribute("menuIdx", 0);
            return "admin/admin_store_registration"; // 관리자 페이지의 첫 메뉴로 이동
        }
        else {
            log.error("알 수 없는 오류");
            throw new CustomException(ErrorCode.SOMETHING_WENT_WRONG);
        }
    }

    @GetMapping("/store")
    public String viewStoreInfo(
            @RequestHeader(value="x-gateway-member-role", required=false) String memberRole,
            @CookieValue(value="accessToken", required=false) String accessToken,
            @RequestParam("id") Long storeId,
            Model model
    ) {
        //  temp: API Gateway 임시 대체
        String role = null;
        if (accessToken != null) role = jwtUtil.getClaims(accessToken).get("role").toString();

        log.info("requested role: {}", role);
        model.addAttribute("memberRole", role); // null 또는 MEMBER 또는 MANAGER(미리보기)

        // 미리보기 모드 플래그
        boolean isPreview = false;
        if (role != null) isPreview = role.equals("MANAGER");
        model.addAttribute("isPreview", isPreview);

        Store store = storeService.getStoreInfo(storeId);
        model.addAttribute("store", store);

        return "store/store_info";
    }

    /* [운영자] 예약 승인 / 예약 거절 페이지 */
    @GetMapping("/store/reserve")
    public String viewStoreReservationPage(
            @RequestHeader(value="x-gateway-member-role", required=false) String memberRole,
            @CookieValue(value="accessToken", required=false) String accessToken,
            Model model
    ) {
        //  temp: API Gateway 임시 대체
        // 운영자 페이지이므로 role은 무조건 MANAGER여야 함
        String role = jwtUtil.getClaims(accessToken).get("role").toString();
        Long code = Long.parseLong(jwtUtil.getClaims(accessToken).getSubject());

        log.info("requested role: {}", role);
        model.addAttribute("memberRole", role);

        Store store = memberService.getMemberInfo(code).getStore();
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
        //  temp: API Gateway 임시 대체
        // 운영자 페이지이므로 role은 무조건 MANAGER여야 함
        String role = jwtUtil.getClaims(accessToken).get("role").toString();
        Long code = Long.parseLong(jwtUtil.getClaims(accessToken).getSubject());

        log.info("requested role: {}", role);
        model.addAttribute("memberRole", role);

        Store store = memberService.getMemberInfo(code).getStore();
        model.addAttribute("storeName", store.getStoreName());
        // 해당 페이지는 테이블에서 보여줄 memberName도 필요
        model.addAttribute("memberName", store.getMember().getMemberName());

        model.addAttribute("menuIdx", 5);

        return "manager/manager_store_reservation_log";
    }
}
