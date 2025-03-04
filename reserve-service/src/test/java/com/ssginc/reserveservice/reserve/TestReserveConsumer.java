package com.ssginc.reserveservice.reserve;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @author Queue-ri
 */

@Component
public class TestReserveConsumer {
    @KafkaListener(topics = "doki-reserve", groupId = "doki")
    public void consume(String message) {
        System.out.println("Consumed message: " + message);
    }
}
