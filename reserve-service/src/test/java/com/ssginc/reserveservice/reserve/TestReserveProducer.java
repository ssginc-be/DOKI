package com.ssginc.reserveservice.reserve;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Queue-ri
 */

@Component
public class TestReserveProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TestReserveProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message) { // "[Kafka] 팝업스토어 예약 topic 정상 작동."
        System.out.println("Produce message: " + message);
        kafkaTemplate.send("doki-reserve", message);
    }
}
