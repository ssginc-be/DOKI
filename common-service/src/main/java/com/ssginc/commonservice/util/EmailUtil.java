package com.ssginc.commonservice.util;

import com.ssginc.commonservice.exception.CustomException;
import com.ssginc.commonservice.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.model.MessageType;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * @author Queue-ri
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailUtil {
    /*
        6자리 정수형 난수 생성, 회원가입 이메일 인증코드 발송
    */

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String MAIL_USERNAME;

    // 난수 생성기 설정
    private static final int SALT_LENGTH = 32;
    private static SecureRandom secureRandom;

    static {
        try {
            secureRandom = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            log.error("SecureRandom 인스턴스 생성 실패");
        }
    }

    /* 인증번호 6자리 난수 생성 함수 */
    public String createEmailAuthKey() {
        secureRandom = new SecureRandom();
        // 휴대폰 인증 용도의 6자리 정수형 key 생성
        int generatedKey = 100000 + secureRandom.nextInt(900000);
        log.info("generated key: {}", generatedKey);

        return String.valueOf(generatedKey);
    }


    /* 회원가입 인증코드 이메일 발송 함수 */
    public void sendEmailWithAuthCode(String toEmail, String generatedKey) throws MessagingException {
        // 이메일 발송
        MimeMessage message = mailSender.createMimeMessage();
        // true: 멀티파트 메세지
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(MAIL_USERNAME); // 발송 메일 주소
        helper.setTo(toEmail); // 수신 메일 주소
        helper.setSubject("[DOKI] 회원가입 이메일 인증 코드"); // 메일 제목
        // 메일 내용 (HTML 가능)
        helper.setText(
                "<h1>[DOKI] 이메일 인증</h1>" +
                        "<p>아래 인증 코드를 회원가입 창에 입력하세요.</p>" +
                        "<h2>" + generatedKey + "</h2>",
                true
        );

        mailSender.send(message);
    }

}
