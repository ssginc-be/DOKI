package com.ssginc.commonservice.reserve.model;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * @author Queue-ri
 */

public interface ReservationEntryRepository extends JpaRepository<ReservationEntry, Long> {

    // Pessimistic Lock 걸고 예약 엔트리 접근
    @Lock(LockModeType.PESSIMISTIC_WRITE) // 다른 트랜잭션에서 읽기, 쓰기, 삭제 방지
    Optional<ReservationEntry> findById(Long id);

    // MySQL 디폴트 설정: REPEATABLE_READ
    @Query("SELECT re FROM ReservationEntry re WHERE re.reservationEntryId = :id")
    Optional<ReservationEntry> findByIdNoLock(@Param("id") Long id);

    List<ReservationEntry> findAllByStore_StoreIdAndEntryDate(Long storeId, LocalDate entryDate);
}
