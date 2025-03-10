package com.ssginc.commonservice.auth.service;

import com.ssginc.commonservice.auth.dto.SignInRequestDto;
import com.ssginc.commonservice.auth.dto.SignUpRequestDto;
import com.ssginc.commonservice.auth.model.PhoneCodeRedisRepository;
import com.ssginc.commonservice.auth.model.RedisPhoneValidationCode;
import com.ssginc.commonservice.auth.model.RedisRefreshToken;
import com.ssginc.commonservice.auth.model.TokenRedisRepository;
import com.ssginc.commonservice.exception.CustomException;
import com.ssginc.commonservice.exception.ErrorCode;
import com.ssginc.commonservice.member.model.Member;
import com.ssginc.commonservice.member.model.MemberRepository;
import com.ssginc.commonservice.util.CookieUtil;
import com.ssginc.commonservice.util.JwtUtil;
import com.ssginc.commonservice.util.SmsUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author Queue-ri
 */

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {
    /*
        sign up, sign in, sign out, validate + parse, refresh
    */
    /*
        [이용자] 회원가입 휴대폰 인증코드 발송, 휴대폰 인증코드 확인
    */
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final SmsUtil smsUtil;

    private final TokenRedisRepository tRedisRepo;
    private final PhoneCodeRedisRepository pcRedisRepo;
    private final MemberRepository mRepo;

    private final PasswordEncoder passwordEncoder;  // BCrypt 인코더 사용


    /* 회원가입 */
    public ResponseEntity<?> signUp(SignUpRequestDto dto) {
        // 이미 가입되어 있는 회원이면 409
        Optional<Member> optMember = mRepo.findByMemberID(dto.getMemberId());
        if (optMember.isPresent()) {
            throw new CustomException(ErrorCode.HAS_EMAIL);
        }

        // 가입 이력이 없으면
        log.info(dto.toString());
        try {
            Member member = new Member();
            member.setMemberId(dto.getMemberId());
            member.setMemberPw(passwordEncoder.encode(dto.getMemberPw()));
            member.setMemberName(dto.getMemberName());
            member.setMemberBirth(dto.getMemberBirth());
            member.setMemberGender(Member.MemberGender.valueOf(dto.getMemberGender()));
            member.setMemberRole(Member.MemberRole.valueOf("MEMBER"));
            member.setMemberPhone(dto.getMemberPhone());
            mRepo.save(member);

        } catch (Exception e) {
            e.printStackTrace(); // custom exception 때문에 상세 오류 못보므로 임시 trace
            log.error("회원가입 정보의 포맷이 잘못됨");
            throw new CustomException(ErrorCode.INVALID_FORMAT);
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /* 로그인 v1 - dto 패스워드를 해싱해서 where 절로 조회 */
//    public ResponseEntity<?> signInV1(SignInRequestDto dto) {
//        log.info(dto.toString());
//        // 회원 정보 가져오기
//        // 조회 결과 없으면 401
//        String hashedPassword = passwordEncoder.encode(dto.getMemberPw());
//        Optional<Member> optMember = mRepo.findByMemberIDAndMemberPw(dto.getMemberId(), hashedPassword);
//        if (optMember.isEmpty()) {
//            throw new CustomException(ErrorCode.USER_NOT_FOUND);
//        }
//
//        // 회원 정보가 조회되면
//        // JWT 발급
//        String accessToken = jwtUtil.generateAccessToken(dto.getMemberId(), "MEMBER", 1000L * 60 * 30); // 30 min
//        String refreshToken = jwtUtil.generateRefreshToken(1000L * 60 * 60 * 6); // 6 hr
//
//        // 헤더에 들어감
//        Long accessMaxAge = 60L * 30; // 30 min
//        Long refreshMaxAge = 60L * 60 * 6; // 6 hr
//        ResponseCookie cookie1 = cookieUtil.generateAccessTokenCookie(accessToken, accessMaxAge);
//        ResponseCookie cookie2 = cookieUtil.generateRefreshTokenCookie(refreshToken, refreshMaxAge);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Set-Cookie", cookie1.toString());
//        headers.set("Set-Cookie", cookie2.toString());
//
//        return ResponseEntity.status(HttpStatus.OK)
//                .headers(headers)
//                .build();
//    }

    /* 로그인 v2 - encode로 해시 비교해서 판단 */
    public ResponseEntity<?> signInV2(SignInRequestDto dto) {
        log.info(dto.toString());
        // 회원 정보 가져오기
        // 조회 결과 없으면 401
        Optional<Member> optMember = mRepo.findByMemberID(dto.getMemberId());
        if (optMember.isEmpty()) {
            log.error("로그인 id 조회 결과 없음");
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        if (!passwordEncoder.matches(dto.getMemberPw(), optMember.get().getMemberPw())) {
            log.error("로그인 패스워드 불일치");
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        // 회원 정보가 조회되면
        // JWT 발급
        Long memberCode = optMember.get().getMemberCode();
        String memberRole = optMember.get().getMemberRole().toString();
        String accessToken = jwtUtil.generateAccessToken(memberCode, memberRole, 1000L * 60 * 30); // 30 min
        String refreshToken = jwtUtil.generateRefreshToken(1000L * 60 * 60 * 6); // 6 hr

        // 헤더에 들어감
        Long accessMaxAge = 60L * 30; // 30 min
        Long refreshMaxAge = 60L * 60 * 6; // 6 hr
        ResponseCookie cookie1 = cookieUtil.generateAccessTokenCookie(accessToken, accessMaxAge);
        ResponseCookie cookie2 = cookieUtil.generateRefreshTokenCookie(refreshToken, refreshMaxAge);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookie1.toString());
        headers.add(HttpHeaders.SET_COOKIE, cookie2.toString());

        // redis에 RT 등록
        tRedisRepo.save(RedisRefreshToken.builder()
                .memberCode(memberCode)
                .refreshToken(refreshToken)
                .expiration(jwtUtil.getExpirationTime(refreshToken))
                .build()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .headers(headers)
                .build();
    }

    /* 로그아웃 */
    public ResponseEntity<?> signOut(Long memberCode) {
        // AT, RT 만료시키기
        ResponseCookie cookie1 = cookieUtil.expireAccessTokenCookie();
        ResponseCookie cookie2 = cookieUtil.expireRefreshTokenCookie();

        // 헤더에 쿠키 만료시키도록 세팅
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookie1.toString());
        headers.add(HttpHeaders.SET_COOKIE, cookie2.toString());

        // Redis에서 member code에 매칭되는 RT 삭제
        try {
            tRedisRepo.deleteById(memberCode);

        } catch (Exception e) {
            e.printStackTrace();
            log.warn("[Redis] memberCode의 refreshToken이 없음"); // critical하지 않음
        }

        return ResponseEntity.status(HttpStatus.OK)
                .headers(headers)
                .build();
    }

    /* 검증 + 회원정보 파싱 */
    // API Gateway에서 접근하는 로직
    public ResponseEntity<?> validateAndParse(String accessToken) {
        Claims claims = null;

        // 토큰 유효성 검증
        try {
            claims = jwtUtil.getClaims(accessToken);
        } catch (Exception e) {
            log.error("토큰이 유효하지 않음");
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        // 유효할 경우 DB에서 회원 정보 조회
        try {
            String memberCodeStr = claims.getSubject();
            Member member = mRepo.findByMemberCode(Long.parseLong(memberCodeStr)).get();
            return new ResponseEntity<>(member, HttpStatus.OK); // 내부 통신이라 굳이 dto 필요없을 듯

        } catch (Exception e) {
            e.printStackTrace();
            log.error("토큰이 유효하지 않음");
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
    }

    /* AT, RT 재발급 */
    public ResponseEntity<?> refreshAccessToken(String refreshToken) {
        // redis에서 해당 토큰의 member code 조회
        Optional<RedisRefreshToken> optToken = tRedisRepo.findByRefreshToken(refreshToken);

        // 조회된 RT 없으면
        if (optToken.isEmpty()) {
            log.error("[Redis] refreshToken 조회 결과 없음");
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        // RT 조회되면
        // 모든 JWT 재발급
        Long memberCode = optToken.get().getMemberCode();
        Optional<Member> optMember = mRepo.findById(memberCode);
        String memberRole = optMember.get().getMemberRole().toString();

        // redis에 토큰이 없으면 member를 조회할 일이 없으므로 하단의 if 문은 무조건 실행되지 않음
//        if (optMember.isEmpty()) {
//            log.error("memberCode 조회 결과 없음");
//            throw new CustomException(ErrorCode.USER_NOT_FOUND);
//        }

        String accessToken = jwtUtil.generateAccessToken(memberCode, memberRole, 1000L * 60 * 30); // 30 min
        String newRefreshToken = jwtUtil.generateRefreshToken(1000L * 60 * 60 * 6); // 6 hr

        // 헤더에 들어감
        Long accessMaxAge = 60L * 30; // 30 min
        Long refreshMaxAge = 60L * 60 * 6; // 6 hr
        ResponseCookie cookie1 = cookieUtil.generateAccessTokenCookie(accessToken, accessMaxAge);
        ResponseCookie cookie2 = cookieUtil.generateRefreshTokenCookie(newRefreshToken, refreshMaxAge);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookie1.toString());
        headers.add(HttpHeaders.SET_COOKIE, cookie2.toString());

        // redis에서 기존 RT 삭제
        try {
            tRedisRepo.deleteById(memberCode);

        } catch (Exception e) {
            e.printStackTrace();
            log.warn("[Redis] memberCode의 refreshToken이 없음"); // critical하지 않음
        }

        // redis에 RT 등록
        tRedisRepo.save(RedisRefreshToken.builder()
                .memberCode(memberCode)
                .refreshToken(newRefreshToken)
                .expiration(jwtUtil.getExpirationTime(newRefreshToken))
                .build()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .headers(headers)
                .build();
    }


    /* [이용자] 회원가입 휴대폰 인증코드 발송 */
    public ResponseEntity<?> sendPhoneValidationCode(String receiverPhoneNum) {
        // 인증번호 생성
        String generatedKey = smsUtil.createSmsAuthKey();
        
        // Redis 저장 - 5분 후에 만료
        pcRedisRepo.save(RedisPhoneValidationCode.builder()
                .validationCode(generatedKey)
                .phoneNum(receiverPhoneNum)
                .build()
        );
        
        SingleMessageSentResponse response = smsUtil.sendSmsAuthMessage(receiverPhoneNum, generatedKey);

//        if (!response.getStatusCode().equals("200")) {
//            log.error("문자 전송 실패");
//            throw new CustomException(ErrorCode.CANNOT_SEND_MESSAGE);
//        }

        return ResponseEntity.ok().body(response);
    }

    /* [이용자] 회원가입 휴대폰 인증코드 확인 */
    public ResponseEntity<?> validatePhoneCode(String phoneNum, String code) {
        Optional<RedisPhoneValidationCode> optRedisCode = pcRedisRepo.findById(code);
        
        if (optRedisCode.isEmpty()) {
            log.warn("인증코드 조회 결과 없음.");
            throw new CustomException(ErrorCode.INVALID_CODE);
        }

        RedisPhoneValidationCode redisCode = optRedisCode.get();
        if (!phoneNum.equals(redisCode.getPhoneNum())) {
            log.warn("유효하지 않은 인증코드");
            throw new CustomException(ErrorCode.INVALID_CODE);
        }

        // 휴대폰 번호 인증 성공
        pcRedisRepo.deleteById(code);

        return ResponseEntity.ok().build();
    }
}
