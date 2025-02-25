package com.ssginc.commonservice.store.controller;

import com.ssginc.commonservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public String viewStoreList(
            @RequestHeader(value="x-gateway-member-role", required=false) String memberRole,
            @CookieValue(value="accessToken", required=false) String accessToken,
            Model model
    ) {
        //  temp: API Gateway 임시 대체
        String role = null;
        if (accessToken != null) role = jwtUtil.getClaims(accessToken).get("role").toString();

        log.info("requested role: {}", role);
        model.addAttribute("memberRole", role);

        if (role == null) return "index"; // 팝업스토어 목록 페이지로 이동
        else if (role.equals("MANAGER")) return "layout/layout-manager"; // 운영자 페이지로 이동
        else if (role.equals("ADMIN")) return "layout/layout-admin"; // 관리자 페이지로 이동
        else return "index"; // 사실상 404 페이지로 가야 함
    }

    @GetMapping("/store")
    public String viewStoreInfo(
            @RequestHeader("x-gateway-member-role") String memberRole,
            @RequestParam Long id,
            Model model
    ) {
        model.addAttribute("memberRole", memberRole);
        model.addAttribute("storeId", id);

        return "store/store_info";
    }
}
