package com.ssginc.commonservice.reserve.model;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Queue-ri
 */

public interface ReservationEntryRepository extends JpaRepository<ReservationEntry, Long> {
}
