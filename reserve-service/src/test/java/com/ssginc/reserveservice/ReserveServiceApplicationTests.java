package com.ssginc.reserveservice;

import com.ssginc.reserveservice.reserve.TestReserveProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ReserveServiceApplicationTests {

    @Autowired
    private TestReserveProducer reserveProducer;

    @Test
    void testKafkaReserveTopic() {
        reserveProducer.sendMessage("[Kafka] 팝업스토어 예약 topic 정상 작동.");
    }

}
