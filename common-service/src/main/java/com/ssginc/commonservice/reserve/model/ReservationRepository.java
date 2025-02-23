package com.ssginc.commonservice.reserve.model;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Queue-ri
 */

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
