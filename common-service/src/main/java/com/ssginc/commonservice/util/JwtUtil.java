package com.ssginc.commonservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

/**
 * @author Queue-ri
 */

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String JWT_SECRET;

    // private Long tokenValidTime = 1000L * 60 * 30; // 30min

    @PostConstruct // 라이프사이클 확인
    protected void init() {
        JWT_SECRET = Base64.getEncoder().encodeToString(JWT_SECRET.getBytes());
    }

    public String generateAccessToken(Long memberCode, String memberRole, Long tokenValidTime) {
        // Claims claims = Jwts.claims().setSubject(id);
        // claims.put("role", role);
        Date now = new Date();
        return Jwts.builder()
                .setSubject(memberCode.toString()) // payload
                .claim("role", memberRole)
                .setIssuedAt(now) // generation time
                .setExpiration(new Date(now.getTime() + tokenValidTime)) // expiration time
                .signWith(SignatureAlgorithm.HS512, JWT_SECRET) // algorithm and signature
                .compact();
    }

    public String generateRefreshToken(Long tokenValidTime) {
        // subject 없는 이유: RT 받으면 redis에서 subject 판단하므로
        Date now = new Date();
        return Jwts.builder()
                .setIssuedAt(now) // generation time
                .setExpiration(new Date(now.getTime() + tokenValidTime)) // expiration time
                .signWith(SignatureAlgorithm.HS512, JWT_SECRET) // algorithm and signature
                .compact();
    }

//    // Identify user email from the received token
//    public String getTokenOwner(String token) {
//        Claims claims = Jwts.parser()
//                .setSigningKey(JWT_SECRET)
//                .parseClaimsJws(token)
//                .getBody();
//        return claims.getSubject();
//    }

    // exception 처리는 service에서 함
    public Long getExpirationTime(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(JWT_SECRET)
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration().getTime();
    }

    // exception 처리는 service에서 함
    public Claims getClaims(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(JWT_SECRET)
                .parseClaimsJws(token)
                .getBody();

        return claims;
    }
}
