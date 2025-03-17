package com.ssginc.commonservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssginc.commonservice.auth.dto.SignInRequestDto;
import com.ssginc.commonservice.auth.dto.SignUpRequestDto;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author Queue-ri
 */

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // 여기에 필요한 설정이나 Mock 초기화 로직 추가
    }

    @DisplayName("이용자 회원가입 및 로그인 flow 테스트")
    @Test
    void testSignInAfterSignUp() throws Exception {

        // 1. 회원가입 페이지 이동
        mockMvc.perform(MockMvcRequestBuilders.get("/auth/sign-up"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("auth/sign_up"));

        // 2. 회원가입
        SignUpRequestDto suDto = SignUpRequestDto.builder()
                .memberId("testuser@doki.com")
                .memberPw("Abcd123!")
                .memberName("Test User")
                .memberPhone("01099999999")
                .memberBirth(LocalDate.parse("1990-04-28", DateTimeFormatter.ISO_DATE))
                .memberGender("MALE")
                .build();

        String signUpData = objectMapper.writeValueAsString(suDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/auth/sign-up")
                        .contentType("application/json")
                        .content(signUpData))
                .andExpect(MockMvcResultMatchers.status().isOk());

        // 3. 로그인
        SignInRequestDto siDto = SignInRequestDto.builder()
                .memberId("testuser@doki.com")
                .memberPw("Abcd123!")
                .build();

        String signInData = objectMapper.writeValueAsString(siDto);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/v1/auth/sign-in")
                        .contentType("application/json")
                        .content(signInData))
                .andExpect(MockMvcResultMatchers.status().isOk()) // 리디렉션은 JS에서 처리함.
                .andReturn();

        Cookie accessToken = result.getResponse().getCookie("accessToken");

        mockMvc.perform(MockMvcRequestBuilders.get("/")
                        .cookie(accessToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("memberRole", "MEMBER"));

        // 4. 로그아웃
        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/auth/sign-out")
                .cookie(accessToken))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @DisplayName("운영자 계정 로그인 테스트")
    @Test
    void testManagerLogin() throws Exception {
        // 운영자 계정으로 로그인하고 팝업스토어 정보 페이지로 이동하는지 확인
        String managerSignInData = objectMapper.writeValueAsString(
                SignInRequestDto.builder()
                        .memberId("manager_001")
                        .memberPw("Abcd123!")
                        .build()
        );

        // 로그인
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/v1/auth/sign-in")
                        .contentType("application/json")
                        .content(managerSignInData))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        Cookie accessToken = result.getResponse().getCookie("accessToken");

        // 운영자 로그인 상태에선 우측 상단에 storeName이 렌더링되어야 함.
        mockMvc.perform(MockMvcRequestBuilders.get("/")
                .cookie(accessToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("storeName", "ZEROBASEONE"))
                .andExpect(MockMvcResultMatchers.model().attribute("memberRole", "MANAGER"));

        // 로그아웃
        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/auth/sign-out")
                .cookie(accessToken))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @DisplayName("관리자 계정 로그인 테스트")
    @Test
    void testManagerSignInAndCheckPopupStoreRegistrationPage() throws Exception {
        // 관리자 계정으로 로그인하고 팝업스토어 등록 페이지로 이동하는지 확인
        String adminSignInData = objectMapper.writeValueAsString(
                SignInRequestDto.builder()
                        .memberId("admin")
                        .memberPw("Abcd123!")
                        .build()
        );

        // 로그인
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/v1/auth/sign-in")
                        .contentType("application/json")
                        .content(adminSignInData))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        Cookie accessToken = result.getResponse().getCookie("accessToken");

        mockMvc.perform(MockMvcRequestBuilders.get("/")
                .cookie(accessToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("memberRole", "ADMIN"));
        
        // 로그아웃
        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/auth/sign-out")
                .cookie(accessToken))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}