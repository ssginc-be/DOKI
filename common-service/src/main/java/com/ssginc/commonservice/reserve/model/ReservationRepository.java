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

    /* 이전에 해당 엔트리로 예약 이력 있었는지 검사하는 용도 */
    @Query("""
        
        SELECT r
        FROM Reservation r
        WHERE r.reservationEntry.reservationEntryId = :entryId AND
        r.member.memberCode = :memberCode AND
        r.store.storeId = :storeId AND
        (r.reservationStatus = 'RESERVE_PENDING' OR r.reservationStatus = 'CONFIRMED' OR r.reservationStatus = 'CANCEL_PENDING')
            
    """)
    List<Reservation> findPreviousReservation(@Param("entryId") Long entryId, @Param("memberCode") Long memberCode, @Param("storeId") Long storeId);

    List<Reservation> findByMember_MemberCode(Long memberCode); // Internal

    List<Reservation> findByReservationEntry_ReservationEntryId(Long reservationEntryId); // Internal test

    /* 나의 예약 최근 예약 순 목록 조회 */
    List<Reservation> findByMember_MemberCodeOrderByCreatedAtDesc(Long memberCode);

    List<Reservation> findByStore_StoreId(Long storeId); // Internal

    /* [운영자] 예약 내역 목록 조회 */
    List<Reservation> findAllByStore_StoreId(Long storeId);

    /* [운영자] 예약 현황 카운터 조회 - 전체 예약 승인/거절/취소 수 */
    Long countByStore_StoreIdAndReservationStatus(Long storeId, Reservation.ReservationStatus reservationStatus);
}
