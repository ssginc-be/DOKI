package com.ssginc.commonservice.store.service;

import com.ssginc.commonservice.exception.CustomException;
import com.ssginc.commonservice.exception.ErrorCode;
import com.ssginc.commonservice.reserve.model.Reservation;
import com.ssginc.commonservice.reserve.model.ReservationLog;
import com.ssginc.commonservice.reserve.model.ReservationLogRepository;
import com.ssginc.commonservice.reserve.model.ReservationRepository;
import com.ssginc.commonservice.store.dto.CategoryNoDescDto;
import com.ssginc.commonservice.store.dto.StoreMetaDto;
import com.ssginc.commonservice.store.model.Store;
import com.ssginc.commonservice.store.model.StoreCategory;
import com.ssginc.commonservice.store.model.StoreRepository;
import com.ssginc.commonservice.util.PageResponse;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Queue-ri
 */

@Slf4j
@RequiredArgsConstructor
@Service
public class StoreService {
    /*
        팝업스토어 목록 조회, 예약 승인/거절/취소
    */
    private final StoreRepository sRepo;
    private final ReservationRepository rRepo;
    private final ReservationLogRepository rlRepo;


    /* 팝업스토어 목록 조회 */
    public ResponseEntity<?> getStoreList(Integer pageIdx) {
        // 페이징 크기
        final int FETCH_SIZE = 9;

        // PageRequest 객체 생성
        PageRequest pageRequest = PageRequest.of(pageIdx, FETCH_SIZE, Sort.by("storeStart").descending());

        // 테이블에서 조회
        Page<Store> storePage = sRepo.findAll(pageRequest);

        // store -> dto 및 category -> dto 변환
        List<StoreMetaDto> data = new ArrayList<>();
        for (Store store : storePage.getContent()) {
            // 중계테이블 스키마 -> desc 없는 category dto로 변환
            List<StoreCategory> scList = store.getStoreCategoryList();
            List<CategoryNoDescDto> dtoList = scList.stream().map(CategoryNoDescDto::new).toList();

            data.add(
                    StoreMetaDto.builder()
                            .storeId(store.getStoreId())
                            .categoryList(dtoList)
                            .storeName(store.getStoreName())
                            .storeShortDesc(store.getStoreShortDesc())
                            .storeStart(store.getStoreStart())
                            .storeEnd(store.getStoreEnd())
                            .storeMainThumbnail(store.getStoreImageList().stream()
                                    .filter(img -> img.getStoreImageTag().equals("MAIN_THUMBNAIL")).toList().get(0).getStoreImageLink())
                            .build()
            );
        }

        // 반환할 page 객체 작성
        PageResponse<?> page = PageResponse.builder()
                .data(data)
                .first(storePage.isFirst())
                .last(storePage.isLast())
                .empty(storePage.isEmpty())
                .totalElements(storePage.getTotalElements())
                .totalPages(storePage.getTotalPages())
                .numberOfElements(storePage.getNumberOfElements())
                .pageSize(storePage.getSize())
                .pageNumber(storePage.getNumber())
                .build();

        return ResponseEntity.ok().body(page);
    }


    /* 특정 팝업스토어 상세 정보 조회 */
    // 내부에서만 사용 - API로 구현하지 않음
    public Store getStoreInfo(Long storeId) {
        Optional<Store> optStore = sRepo.findById(storeId);

        if (optStore.isEmpty()) {
            log.error("요청 id의 팝업스토어 조회 결과 없음");
            return null;
        }

        return optStore.get();
    }
    
    
    /* 팝업스토어 예약 승인 */
    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE) // 다른 트랜잭션에서 읽기, 쓰기, 삭제 방지
    public ResponseEntity<?> confirmReservation(Long rid) {
        Optional<Reservation> optReservation = rRepo.findById(rid);
        if (optReservation.isEmpty()) {
            log.error("요청 id의 예약 조회 결과 없음");
            throw new CustomException(ErrorCode.RESERVATION_NOT_FOUND);
        }

        // 예약 정원 check
        Reservation reservation = optReservation.get();
        int capacity = reservation.getReservationEntry().getCapacity();
        int reservedCount = reservation.getReservationEntry().getReservedCount();
        int headcount = reservation.getHeadcount();
        if (reservedCount + headcount > capacity) {
            log.warn("정원을 초과하여 승인할 수 없음");
            throw new CustomException(ErrorCode.EXCEED_RESERVATION_CAPACITY);
        }

        // 정원 내면 reservedCount 업데이트
        reservation.getReservationEntry().setReservedCount(reservedCount + headcount);

        // 예약 내역 상태 업데이트
        reservation.setReservationStatus(Reservation.ReservationStatus.CONFIRMED); // UPDATE

        // 예약 상태변경 로그 작성
        ReservationLog rlog = ReservationLog.builder()
                .reservation(reservation)
                .reserveMethod(ReservationLog.ReserveMethod.V1)
                .reservationStatus(ReservationLog.ReservationStatus.CONFIRMED)
                .build();
        rlRepo.save(rlog);

        return ResponseEntity.ok().build();
    }

    /* 팝업스토어 예약 거절 */
    @Transactional
    public ResponseEntity<?> refuseReservation(Long rid) {
        Optional<Reservation> optReservation = rRepo.findById(rid);
        if (optReservation.isEmpty()) {
            log.error("요청 id의 예약 조회 결과 없음");
            throw new CustomException(ErrorCode.RESERVATION_NOT_FOUND);
        }

        // 예약 내역 상태 업데이트
        Reservation reservation = optReservation.get();
        reservation.setReservationStatus(Reservation.ReservationStatus.REFUSED); // UPDATE

        // 예약 상태변경 로그 작성
        ReservationLog rlog = ReservationLog.builder()
                .reservation(reservation)
                .reserveMethod(ReservationLog.ReserveMethod.V1)
                .reservationStatus(ReservationLog.ReservationStatus.REFUSED)
                .build();
        rlRepo.save(rlog);

        return ResponseEntity.ok().build();
    }

    /* 팝업스토어 예약 취소 */
    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE) // 다른 트랜잭션에서 읽기, 쓰기, 삭제 방지
    public ResponseEntity<?> cancelReservation(Long rid) {
        Optional<Reservation> optReservation = rRepo.findById(rid);
        if (optReservation.isEmpty()) {
            log.error("요청 id의 예약 조회 결과 없음");
            throw new CustomException(ErrorCode.RESERVATION_NOT_FOUND);
        }

        // 정원 내면 reservedCount 업데이트
        Reservation reservation = optReservation.get();
        int reservedCount = reservation.getReservationEntry().getReservedCount();
        int headcount = reservation.getHeadcount();
        reservation.getReservationEntry().setReservedCount(reservedCount - headcount);

        // 예약 내역 상태 업데이트
        reservation.setReservationStatus(Reservation.ReservationStatus.CANCELED); // UPDATE

        // 예약 상태변경 로그 작성
        ReservationLog rlog = ReservationLog.builder()
                .reservation(reservation)
                .reserveMethod(ReservationLog.ReserveMethod.V1)
                .reservationStatus(ReservationLog.ReservationStatus.CANCELED)
                .build();
        rlRepo.save(rlog);

        return ResponseEntity.ok().build();
    }
}
