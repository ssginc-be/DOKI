package com.ssginc.commonservice.reserve.controller;

import com.ssginc.commonservice.member.model.Member;
import com.ssginc.commonservice.member.service.MemberService;
import com.ssginc.commonservice.store.model.Store;
import com.ssginc.commonservice.store.service.StoreService;
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
@RequestMapping("/reserve")
public class ReservationController {
    /*
        view reservation page
    */
    private final JwtUtil jwtUtil;
    private final StoreService storeService;
    private final MemberService memberService;

    @GetMapping
    public String viewReservationPage(
            @RequestHeader(value="x-gateway-member-role", required=false) String memberRole,
            @CookieValue(value="accessToken", required=false) String accessToken,
            @RequestParam(value="id") Long storeId,
            Model model
    ) {
        //  temp: API Gateway 임시 대체
        // 예약 페이지 진입 시점에서 role은 무조건 MEMBER여야 함
        String role = jwtUtil.getClaims(accessToken).get("role").toString();
        Long code = Long.parseLong(jwtUtil.getClaims(accessToken).getSubject());

        log.info("requested role: {}", role);
        model.addAttribute("memberRole", role);

        /* store info, store reservation setting, member 데이터 가져오기 */
        Store store = storeService.getStoreInfo(storeId);
        model.addAttribute("store", store);

        // 여기는 일단 땜빵
        // List<ReservationEntry> entryList = storeService.getReservationEntries(storeId);
        // model.addAttribute("entryList", entryList);

        Member member = memberService.getMemberInfo(code);
        model.addAttribute("member", member);

        return "reserve/store-reservation";
    }
}
