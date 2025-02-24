package com.ssginc.commonservice.store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Queue-ri
 */

@RequiredArgsConstructor
@Controller
@RequestMapping("/") // root 경로가 곧 팝업스토어 목록 조회 페이지
public class StoreController {
    /*
        store list, store info
    */
    @GetMapping
    public String viewStoreList(
            @RequestHeader(value="x-gateway-member-role", required=false) String memberRole,
            Model model
    ) {
        model.addAttribute("memberRole", memberRole);

        return "index";
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
