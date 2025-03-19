package com.ssginc.commonservice;

import com.ssginc.commonservice.backdoor.TestReserveService;
import com.ssginc.commonservice.member.model.Member;
import com.ssginc.commonservice.member.model.MemberRepository;
import com.ssginc.commonservice.reserve.dto.ReserveRequestDto;
import com.ssginc.commonservice.reserve.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Queue-ri
 */

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.jpa.show-sql=false", // 없으면 컴 터짐
        "spring.jpa.properties.hibernate.show_sql=false", // 없으면 컴 터짐
        "spring.jpa.properties.hibernate.format_sql=false", // 없으면 컴 터짐
        "logging.level.org.hibernate.SQL=OFF",  // 없으면 컴 터짐
        "spring.jpa.properties.hibernate.jdbc.batch_size=1000",  // 한 번에 처리할 배치 크기
        "spring.jpa.properties.hibernate.order_inserts=true",    // 삽입 순서 지정
        "spring.jpa.properties.hibernate.order_updates=true",    // 업데이트 순서 지정
        "spring.jpa.properties.hibernate.flush.mode=COMMIT"    // 커밋 시점에 flush
})
public class ReserveRestControllerV2Test {
    /*
        V2 예약 동시성 테스트
    */

    @Autowired
    private ReservationRepository rRepo;

    @Autowired
    private ReservationEntryRepository reRepo;

    @Autowired
    private MemberRepository mRepo;

    @Autowired
    private TestReserveService testReserveService; // 실제 service에 mock 객체 주입

    @PersistenceContext
    private EntityManager entityManager;

    private int RESERVATION_TRIAL;

    private Long TARGET_ENTRY_ID;

    private List<Member> memberList;

    @BeforeEach
    public void setUp() {
        // Given: 공통 테스트 데이터
        // 공격할 엔트리는 data.sql 참고
        RESERVATION_TRIAL = 50000; // 개별 테스트 실행은 각 5만씩 가능
        TARGET_ENTRY_ID = 10L;

        log.info("테스트 계정 적재 시작");
        memberList = new ArrayList<>();
        for (long i = 0; i < RESERVATION_TRIAL; ++i) {
            Member member = Member.builder()
                    .memberName("테스트계정")
                    .memberId("TEST_" + i)
                    .memberPw("TEST")
                    .memberPhone("TEST")
                    .memberRole(Member.MemberRole.MEMBER)
                    .build();

            entityManager.persist(member);
            memberList.add(member);
        }
        mRepo.saveAll(memberList);
        log.info("테스트 계정 적재 완료");
    }

    // 1. 단순 @Transactional만 설정한 예약 동작 테스트
    @DisplayName("Pessimistic Lock 예약 테스트")
    @Test
    @Transactional
    //@Sql("/data.sql")  // resources/data.sql 실행
    public void testWithPessimisticLock() throws InterruptedException {
        // 동시성 테스트: 여러 스레드에서 예약 시도
        log.info("테스트 예약 시작");
        int threadCount = RESERVATION_TRIAL;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (Member member : memberList) {
            ReserveRequestDto reserveRequestDto = ReserveRequestDto.builder()
                    .storeId(1L)
                    .entryId(TARGET_ENTRY_ID)
                    .memberCode(member.getMemberCode())
                    .reservedDateTime(LocalDateTime.parse("2099-12-31 00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                    .headcount(1)
                    .build();

            executorService.submit(() -> {
                try {
                    testReserveService.reserve(reserveRequestDto, 1); // Pessimistic Lock Mode
                }  catch (Exception e) {
                    //e.printStackTrace();
                } finally{
                    latch.countDown();
                }
            });
        }
        executorService.shutdown();
        latch.await();
        log.info("테스트 예약 완료");
    }

    // 2. 자바의 synchronized를 이용한 예약 동작 테스트
    @DisplayName("Java synchronized 예약 테스트")
    @Test
    @Transactional
    public void testWithSynchronized() throws InterruptedException {
        // 동시성 테스트: 여러 스레드에서 예약 시도
        log.info("테스트 예약 시작");
        int threadCount = RESERVATION_TRIAL;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (Member member : memberList) {
            ReserveRequestDto reserveRequestDto = ReserveRequestDto.builder()
                    .storeId(1L)
                    .entryId(TARGET_ENTRY_ID)
                    .memberCode(member.getMemberCode())
                    .reservedDateTime(LocalDateTime.parse("2099-12-31 00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                    .headcount(1)
                    .build();

            executorService.submit(() -> {
                try {
                    testReserveService.synchronizedReserve(reserveRequestDto, 2); // Default Mode: REPEATABLE_READ
                }  catch (Exception e) {
                    //e.printStackTrace();
                } finally{
                    latch.countDown();
                }
            });
        }
        executorService.shutdown();
        latch.await();
        log.info("테스트 예약 완료");
    }

    // 3. 비관적 락을 설정한 예약 테스트
    @DisplayName("@Transactional 예약 테스트")
    @Test
    @Transactional
    public void testWithSimpleTransaction() throws InterruptedException {
        // 동시성 테스트: 여러 스레드에서 예약 시도
        log.info("테스트 예약 시작");
        int threadCount = RESERVATION_TRIAL;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (Member member : memberList) {
            ReserveRequestDto reserveRequestDto = ReserveRequestDto.builder()
                    .storeId(1L)
                    .entryId(TARGET_ENTRY_ID)
                    .memberCode(member.getMemberCode())
                    .reservedDateTime(LocalDateTime.parse("2099-12-31 00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                    .headcount(1)
                    .build();

            executorService.submit(() -> {
                try {
                    testReserveService.reserve(reserveRequestDto, 2); // Default Mode: REPEATABLE_READ
                }  catch (Exception e) {
                    //e.printStackTrace();
                } finally{
                    latch.countDown();
                }
            });
        }
        executorService.shutdown();
        latch.await();
        log.info("테스트 예약 완료");
    }

    @AfterEach
    public void showResult() {
        // 예약 정원 초과 여부 검증
        ReservationEntry entry = reRepo.findById(TARGET_ENTRY_ID).get();
        List<Reservation> reservationList = rRepo.findByReservationEntry_ReservationEntryId(TARGET_ENTRY_ID);
        log.info("예약 카운터: {} | 정원: {}", entry.getReservedCount(), entry.getCapacity());
        log.info("예약 데이터 수: {}", reservationList.size());
        log.info("예약 엔트리 상태: {}", entry.getEntryStatus().toString());
        assertEquals("CLOSED", entry.getEntryStatus().toString());  // 엔트리가 닫혔는지 확인
        assertTrue(entry.getReservedCount() <= entry.getCapacity());  // 최대 정원 초과 여부 확인
    }
}