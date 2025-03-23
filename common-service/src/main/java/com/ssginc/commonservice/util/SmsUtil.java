package com.ssginc.commonservice.util;

import com.ssginc.commonservice.exception.CustomException;
import com.ssginc.commonservice.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.model.MessageType;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * @author Queue-ri
 */

@Slf4j
@Component
public class SmsUtil {
    /*
        6자리 정수형 난수 생성, 회원가입 휴대폰 인증코드 발송
    */

    @Value("${coolsms.sender}")
    private String SMS_SENDER; // 발신 번호

    @Value("${coolsms.enable}")
    private boolean SMS_ENABLED; // false면 로그만 찍히고 실제 문자 전송은 스킵됨

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

    // CoolSMS API 설정
    private final DefaultMessageService messageService;

    @Autowired
    public SmsUtil(@Value("${coolsms.apiKey}") String SMS_API_KEY, @Value("${coolsms.secret}") String SMS_SECRET) {
        this.messageService = NurigoApp.INSTANCE.initialize(SMS_API_KEY, SMS_SECRET, "https://api.coolsms.co.kr");
    }

    /* 인증번호 6자리 난수 생성 함수 */
    public String createSmsAuthKey() {
        secureRandom = new SecureRandom();
        // 휴대폰 인증 용도의 6자리 정수형 key 생성
        int generatedKey = 100000 + secureRandom.nextInt(900000);
        log.info("generated key: {}", generatedKey);

        return String.valueOf(generatedKey);
    }


    /* 회원가입 휴대폰 인증 함수 */
    public SingleMessageSentResponse sendSmsAuthMessage(String receiver, String generatedKey){
        Message message = new Message();
        message.setFrom(SMS_SENDER); // 발신 번호
        message.setTo(receiver); // 수신 번호
        message.setText("[DOKI] 인증번호: " + generatedKey + "를 입력하세요.");

        log.info("Message to send: {}", message.getText());

        // mock response
        SingleMessageSentResponse response = new SingleMessageSentResponse(
                "", receiver, SMS_SENDER, MessageType.SMS, "[DEBUG MODE] Message not sent.", "", "",  "200", ""
        );
        if (SMS_ENABLED) {
            // 실제 발송 시 response를 교체
            response = messageService.sendOne(new SingleMessageSendingRequest(message));
            log.info("CoolSMS response: {}", response);
        }

        return response;
    }

    /* 회원가입 휴대폰 인증 함수 */
    public SingleMessageSentResponse sendReserveConfirmedMessage(String receiver, String memberName, String storeName, LocalDate reservedDate, LocalTime reservedTime) {
        Message message = new Message();
        message.setFrom(SMS_SENDER); // 발신 번호
        message.setTo(receiver); // 수신 번호
        String content = "[DOKI 예약 알림]\n\n" + memberName + " 고객님의 예약이 확정되었습니다.\n\n■ 팝업스토어명: " + storeName + "\n■ 예약일: " + reservedDate + "\n■ 예약시간: "+ reservedTime;
        message.setText(content);

        log.info("Message to send: {}", message.getText());

        // mock response
        SingleMessageSentResponse response = new SingleMessageSentResponse(
                "", receiver, SMS_SENDER, MessageType.SMS, "[DEBUG MODE] Message not sent.", "", "",  "200", ""
        );
        if (SMS_ENABLED) {
            // 실제 발송 시 response를 교체
            response = messageService.sendOne(new SingleMessageSendingRequest(message));
            log.info("CoolSMS response: {}", response);
        }

        return response;
    }

}
