package com.ssginc.commonservice.reserve.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * @author Queue-ri
 */

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query(
            "SELECT r " +
            "FROM Reservation r " +
            "WHERE r.reservationEntry.reservationEntryId = :entryId AND " +
            "r.member.memberCode = :memberCode AND " +
            "r.store.storeId = :storeId AND " +
            "(r.reservationStatus = 'RESERVE_PENDING' OR r.reservationStatus = 'CONFIRMED')"
    )
    Optional<List<Reservation>> findPreviousReservation(@Param("entryId") Long entryId, @Param("memberCode") Long memberCode, @Param("storeId")Long storeId);
}
