package com.ssginc.commonservice.member.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/**
 * @author Queue-ri
 */

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("select m from Member m where m.memberCode = :memberCode")
    Optional<Member> findByMemberCode(Long memberCode); // 내부 조회용 id

    @Query("select m from Member m where m.memberId = :memberId")
    Optional<Member> findByMemberID(String memberId); // ID이자 이메일

//    @Query("select m from Member m where m.memberId = :memberId and m.memberPw = :memberPw")
//    Optional<Member> findByMemberIDAndMemberPw(String memberId, String memberPw);
}
