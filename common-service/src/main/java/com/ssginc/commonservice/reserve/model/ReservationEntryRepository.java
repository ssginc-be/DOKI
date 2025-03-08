package com.ssginc.commonservice.reserve.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Queue-ri
 */

public interface ReservationEntryRepository extends JpaRepository<ReservationEntry, Long> {
    List<ReservationEntry> findAllByStore_StoreIdAndEntryDate(Long storeId, LocalDate entryDate);
}
