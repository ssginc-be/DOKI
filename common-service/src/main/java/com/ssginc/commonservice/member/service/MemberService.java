package com.ssginc.commonservice.member.service;

import com.ssginc.commonservice.exception.CustomException;
import com.ssginc.commonservice.exception.ErrorCode;
import com.ssginc.commonservice.member.model.Member;
import com.ssginc.commonservice.member.model.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author Queue-ri
 */

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {
    /*
        회원 정보 조회
    */
    private final MemberRepository mRepo;


    /* 회원 정보 조회 - 내부에서만 사용하고 API는 없음 */
    public Member getMemberInfo(Long memberCode) {
        Optional<Member> optMember = mRepo.findById(memberCode);

        if (optMember.isEmpty()) {
            log.info("요청 code의 회원 조회 결과 없음.");
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        return optMember.get();
    }
}
