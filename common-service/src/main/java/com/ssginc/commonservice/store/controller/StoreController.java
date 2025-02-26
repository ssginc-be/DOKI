package com.ssginc.commonservice.store.controller;

import com.ssginc.commonservice.exception.CustomException;
import com.ssginc.commonservice.exception.ErrorCode;
import com.ssginc.commonservice.store.document.StoreMetaDocument;
import com.ssginc.commonservice.store.dto.StoreMetaDto;
import com.ssginc.commonservice.store.model.Store;
import com.ssginc.commonservice.store.service.StoreIndexService;
import com.ssginc.commonservice.store.service.StoreService;
import com.ssginc.commonservice.util.JwtUtil;
import com.ssginc.commonservice.util.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author Queue-ri
 */

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/") // root 경로가 곧 팝업스토어 목록 조회 페이지
public class StoreController {
    /*
        store list, store info
    */
    private final JwtUtil jwtUtil;
    private final StoreService storeService;
    private final StoreIndexService storeIndexService; // Elasticsearch

    @GetMapping
    public String viewStoreList(
            @RequestHeader(value="x-gateway-member-role", required=false) String memberRole,
            @CookieValue(value="accessToken", required=false) String accessToken,
            @RequestParam(value="page", required = false) Integer pageIdx,
            Model model
    ) {
        //  temp: API Gateway 임시 대체
        String role = null;
        if (accessToken != null) role = jwtUtil.getClaims(accessToken).get("role").toString();

        log.info("requested role: {}", role);
        model.addAttribute("memberRole", role);

        if (role == null || role.equals("MEMBER")) { // 비회원이거나 로그인한 이용자 (null 체크가 맨 위에 있어야 함)
            // role.equals("MEMBER")
            if (pageIdx == null) pageIdx = 0; // 루트 경로에서 호출 시 첫 페이지 조회
            PageResponse page = storeIndexService.getStoreListInternal(pageIdx); // v2 팝업스토어 조회

            model.addAttribute("page", page);
            List<StoreMetaDocument> storeList = (List<StoreMetaDocument>) page.getData(); // downcast
            model.addAttribute("storeList", storeList);
            model.addAttribute("formatter", DateTimeFormatter.ofPattern("MM.dd(E)"));

            System.out.println(page);
            System.out.println(storeList);

            return "index"; // 팝업스토어 목록 페이지로 이동
        }
        else if (role.equals("MANAGER")) return "layout/layout-manager"; // 운영자 페이지로 이동
        else if (role.equals("ADMIN")) return "layout/layout-admin"; // 관리자 페이지로 이동
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
        model.addAttribute("memberRole", role); // null 또는 MEMBER

        Store store = storeService.getStoreInfo(storeId);
        model.addAttribute("store", store);

        return "store/store_info";
    }
}
