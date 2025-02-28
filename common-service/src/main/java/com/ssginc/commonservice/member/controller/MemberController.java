package com.ssginc.commonservice.member.controller;

import com.ssginc.commonservice.member.model.Member;
import com.ssginc.commonservice.member.service.MemberService;
import com.ssginc.commonservice.reserve.model.Reservation;
import com.ssginc.commonservice.reserve.service.ReserveService;
import com.ssginc.commonservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Queue-ri
 */

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/member")
public class MemberController {
    /*
        view member reservation page - '나의 예약' 페이지 조회
    */
    private final JwtUtil jwtUtil;
    private final ReserveService reserveService;
    private final MemberService memberService;

    @GetMapping("/reserve")
    public String viewMyReservationPage(
            @RequestHeader(value="x-gateway-member-role", required=false) String memberRole,
            @CookieValue(value="accessToken", required=false) String accessToken,
            Model model
    ) {
        //  temp: API Gateway 임시 대체
        // '나의 예약' 페이지 진입 시점에서 role은 무조건 MEMBER여야 함
        String role = jwtUtil.getClaims(accessToken).get("role").toString();
        Long code = Long.parseLong(jwtUtil.getClaims(accessToken).getSubject());

        log.info("requested role: {}", role);
        model.addAttribute("memberRole", role);

        log.info("requested role: {}", role);
        model.addAttribute("memberRole", role);

        /* 이용자의 reservation 목록 가져오기 */
        Member member = memberService.getMemberInfo(code);
        model.addAttribute("member", member);

        List<Reservation> reservationList = reserveService.getMemberReservations(code);
        model.addAttribute("reservationList", reservationList);

        return "member/my_reservation";
    }
}
