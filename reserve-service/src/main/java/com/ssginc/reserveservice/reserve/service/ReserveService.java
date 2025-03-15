package com.ssginc.reserveservice.reserve.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssginc.reserveservice.exception.CustomException;
import com.ssginc.reserveservice.exception.ErrorCode;
import com.ssginc.reserveservice.reserve.dto.ReserveRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * @author Queue-ri
 */

@Slf4j
@RequiredArgsConstructor
@Service
public class ReserveService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /* [V2 자동승인] 예약 요청 시 Kafka 메시지 발행하는 함수 */
    public ResponseEntity<?> sendMessage(ReserveRequestDto dto) {
        String message = null;
        try {
            message = objectMapper.writeValueAsString(dto);
        } catch (Exception e) {
            log.error("예약 요청 message 변환 실패");
            throw new CustomException(ErrorCode.SOMETHING_WENT_WRONG);
        }
        log.info(message);
        kafkaTemplate.send("doki-reserve", message);

        return ResponseEntity.ok().build();
    }
}
