package com.ssginc.commonservice.store.service;

import com.ssginc.commonservice.exception.CustomException;
import com.ssginc.commonservice.exception.ErrorCode;
import com.ssginc.commonservice.member.model.Member;
import com.ssginc.commonservice.member.model.MemberRepository;
import com.ssginc.commonservice.notification.service.NotificationService;
import com.ssginc.commonservice.reserve.model.*;
import com.ssginc.commonservice.store.dto.*;
import com.ssginc.commonservice.store.model.*;
import com.ssginc.commonservice.util.PageResponseDto;
import com.ssginc.commonservice.util.S3Uploader;
import com.ssginc.commonservice.util.SmsUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
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
        팝업스토어 목록 조회, 팝업스토어 카테고리 목록 조회, 예약 승인/거절/취소, 예약 엔트리 조회, 팝업스토어 등록
    */
    /*
        [운영자] 예약 내역 목록 조회,
        [운영자] 예약 현황 카운터 조회 (메트릭 영역)
    */
    /*
        [운영자] 특정 예약에 대한 예약 상태 변경 로그 조회
    */
    private final MemberRepository mRepo;
    private final StoreRepository sRepo;
    private final CategoryRepository cRepo;
    private final StoreCategoryRepository scRepo;
    private final StoreImageRepository siRepo;
    private final ReservationRepository rRepo;
    private final ReservationLogRepository rlRepo;
    private final ReservationEntryRepository reRepo;

    private final NotificationService notificationService;
    private final StoreIndexService storeIndexService;

    private final S3Uploader s3Uploader;

    private final EntityManager entityManager;
    private final PasswordEncoder passwordEncoder;  // BCrypt 인코더 사용
    private final SmsUtil smsUtil;

    @Value("${cloud.aws.cloudfront.domain}")
    private String cloudFrontDomain;


    /* 팝업스토어 목록 조회 */
    public ResponseEntity<?> getStoreList(Integer pageIdx) {
        // 페이징 크기
        final int FETCH_SIZE = 9;

        // PageRequest 객체 생성
        if (pageIdx > 0) pageIdx -= 1; // 사용자의 1페이지 == 서버의 0페이지
        PageRequest pageRequest = PageRequest.of(pageIdx, FETCH_SIZE, Sort.by("storeStartDate").descending());

        // 테이블에서 조회
        Page<Store> storePage = sRepo.findAll(pageRequest);
        // 조회 결과를 response dto로 변환
        PageResponseDto<?> page = convertStorePageToMetaDtoPage(storePage);

        return ResponseEntity.ok().body(page);
    }

    /* 팝업스토어 카테고리 목록 조회 */
    public ResponseEntity<?> getStoreListOfSelectedCategory(Long categoryId, Integer pageIdx) {
        // 페이징 크기
        final int FETCH_SIZE = 9;

        // PageRequest 객체 생성
        if (pageIdx > 0) pageIdx -= 1; // 사용자의 1페이지 == 서버의 0페이지
        PageRequest pageRequest = PageRequest.of(pageIdx, FETCH_SIZE, Sort.by("storeStartDate").descending());

        // 테이블에서 조회
        Page<Store> storePage = sRepo.findAllByStoreCategoryList_Category_CategoryId(categoryId, pageRequest);
        // 조회 결과를 response dto로 변환
        PageResponseDto<?> page = convertStorePageToMetaDtoPage(storePage);

        return ResponseEntity.ok().body(page);
    }

    /* 팝업스토어 테이블 조회 결과를 목록 조회용 dto response로 변환하는 함수 */
    private PageResponseDto<?> convertStorePageToMetaDtoPage(Page<Store> storePage) {
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
                            .storeStartDate(store.getStoreStartDate())
                            .storeEndDate(store.getStoreEndDate())
                            .storeMainThumbnail(store.getStoreImageList().stream()
                                    .filter(img -> img.getStoreImageTag().equals("MAIN_THUMBNAIL")).toList().get(0).getStoreImageLink())
                            .build()
            );
        }

        // 반환할 page 객체 작성
        PageResponseDto<?> page = PageResponseDto.builder()
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

        return page;
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
        ReservationEntry entry = reservation.getReservationEntry();
        int capacity = entry.getCapacity();
        int reservedCount = entry.getReservedCount();
        int headcount = reservation.getHeadcount();

        if (reservedCount + headcount > capacity) {
            log.warn("정원을 초과하여 승인할 수 없음");
            throw new CustomException(ErrorCode.EXCEED_RESERVATION_CAPACITY);
        }
        else if (reservedCount + headcount == capacity) {
            // 정원 한도에 도달했으므로 예약 엔트리 status를 CLOSED로 업데이트해야 함.
            entry.setEntryStatus(ReservationEntry.EntryStatus.CLOSED);
        }

        // 정원 내면 reservedCount 업데이트
        entry.setReservedCount(reservedCount + headcount);

        // 예약 내역 상태 업데이트
        reservation.setReservationStatus(Reservation.ReservationStatus.CONFIRMED); // UPDATE

        // 예약 상태변경 로그 작성
        ReservationLog rlog = ReservationLog.builder()
                .reservation(reservation)
                .reserveMethod(ReservationLog.ReserveMethod.V1)
                .reservationStatus(ReservationLog.ReservationStatus.CONFIRMED)
                .build();
        rlRepo.save(rlog);

        // 이용자에게 문자 알림 발송
        smsUtil.sendReserveConfirmedMessage(
                reservation.getMember().getMemberPhone(),
                reservation.getMember().getMemberName(),
                reservation.getStore().getStoreName(),
                entry.getEntryDate(),
                entry.getEntryTime()
        );

        // 운영자 -> 이용자에게 예약 확정 알림
        // 예약 성공했으므로 reservation 엔티티가 생성되었고, 따라서 sid가 아닌 rid를 넘겨주는 것이 맞음.
        notificationService.notifyReserveResultToMember(reservation.getReservationId(), "CONFIRMED");

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

        // 운영자 -> 이용자에게 예약 거절 알림
        // 예약 성공했으므로 reservation 엔티티가 생성되었고, 따라서 sid가 아닌 rid를 넘겨주는 것이 맞음.
        notificationService.notifyReserveResultToMember(reservation.getReservationId(), "REFUSED");

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

        // 정원 내면 entryStatus(조건부) 및 reservedCount(필수) 업데이트
        Reservation reservation = optReservation.get();
        ReservationEntry entry = reservation.getReservationEntry();
        int reservedCount = entry.getReservedCount();
        int headcount = reservation.getHeadcount();
        // 엔트리 status가 CLOSED면 OPEN으로 돌려놓기
        if (entry.getEntryStatus() == ReservationEntry.EntryStatus.CLOSED) {
            entry.setEntryStatus(ReservationEntry.EntryStatus.OPEN);
        }
        // 카운터 업데이트
        entry.setReservedCount(reservedCount - headcount);

        // 예약 내역 상태 업데이트
        reservation.setReservationStatus(Reservation.ReservationStatus.CANCELED); // UPDATE

        // 예약 상태변경 로그 작성
        ReservationLog rlog = ReservationLog.builder()
                .reservation(reservation)
                .reserveMethod(ReservationLog.ReserveMethod.V1)
                .reservationStatus(ReservationLog.ReservationStatus.CANCELED)
                .build();
        rlRepo.save(rlog);

        // 운영자 -> 이용자에게 예약 취소 알림
        // 예약 성공했으므로 reservation 엔티티가 생성되었고, 따라서 sid가 아닌 rid를 넘겨주는 것이 맞음.
        notificationService.notifyReserveResultToMember(reservation.getReservationId(), "CANCELED");

        return ResponseEntity.ok().build();
    }

    /* 특정 팝업스토어의 선택한 날짜에 대한 예약 엔트리 조회 */
    public ResponseEntity<?> getEntriesOfSpecificDate(Long sid, LocalDate date) {
        List<ReservationEntry> reList = reRepo.findAllByStore_StoreIdAndEntryDate(sid, date);

        // entity -> dto 변환
        List<ReservationEntryResponseDto> dtoList = new ArrayList<>();
        for (ReservationEntry entry : reList) {
            ReservationEntryResponseDto dto = ReservationEntryResponseDto.builder()
                    .reservationEntryId(entry.getReservationEntryId())
                    .entryTime(entry.getEntryTime())
                    .entryStatus(entry.getEntryStatus())
                    .build();

            dtoList.add(dto);
        }

        return ResponseEntity.ok().body(dtoList);
    }

    /* 팝업스토어 등록 */
    @Transactional
    public ResponseEntity<?> registerStore(StoreSaveRequestDto dto, List<MultipartFile> mfiles) {
        // 1. Store 엔티티 생성
        // ES 인덱싱때문에 일단 setter로 따로 주입하는데, 주입을 안해도 되는지는 나중에 알아봐야 할 듯
        Store store = Store.builder()
                .storeName(dto.getStoreName())
                //.storeCategoryList(storeCategoryList) // 하단에서 setter로 주입
                .storeBranch(dto.getStoreBranch())
                .storeAt(dto.getStoreAt())
                .storeShortDesc(dto.getStoreShortDesc())
                .storeLongDesc(dto.getStoreLongDesc())
                .storeStartDate(dto.getStoreStartDate())
                .storeEndDate(dto.getStoreEndDate())
                .storeStartTime(dto.getStoreStartTime())
                .storeEndTime(dto.getStoreEndTime())
                .storeReserveMethod(Store.StoreReserveMethod.valueOf(dto.getReserveMethod()))
                .storeReserveGap(dto.getReserveGap())
                .storeCapacity(dto.getCapacity())
                .storeStatus(Store.StoreStatus.valueOf("ACTIVE"))
                //.storeImageList(storeImageList) // 하단에서 setter로 주입
                .build();

        entityManager.persist(store); // 영속화 -> 이 시점부터 store의 id 값을 알 수 있음.


        // 2. 팝업스토어 이미지를 S3에 업로드
        // S3에 업로드 시 자동으로 resize -> 파일명을 convention에 맞게 지정해서 S3에 재업 -> CloudFront에서 배포하는 방식
        // [400x400] MAIN_THUMBNAIL / [174x174] SUB_THUMBNAIL / [450xauto] CONTENT_DETAIL
        // 파일명 convention: 파일명_사이즈 - 추후 리팩토링이 필요한 부분

        // DB에 저장할 최종 이미지 CDN url
        List<String> thumbUrlList = new ArrayList<>();
        List<String> contentUrlList = new ArrayList<>();

        // 파일 이미지 리스트에서 대표 이미지 파일을 추출
        MultipartFile thumbFile = mfiles.get(dto.getThumbIdx());

        String publicThumbUrl1 = cloudFrontDomain + "/resize/thumb/400/";
        String publicThumbUrl2 = cloudFrontDomain + "/resize/thumb/174/";
        String publicContentUrl = cloudFrontDomain + "/resize/content/450/";

        
        // 이미지 업로드
        // MAIN_THUMBNAIL, SUB_THUMBNAIL, CONTENT_DETAIL 생성
        try {
            s3Uploader.uploadStoreMainImage(thumbFile); // MAIN_THUMBNAIL, SUB_THUMBNAIL 생성
            s3Uploader.uploadStoreImages(mfiles); // CONTENT_DETAIL 생성
        } catch (IOException e) {
            log.error("S3 업로드 실패");
            throw new CustomException(ErrorCode.CANNOT_UPLOAD_IMAGE);
        }

        // 3. StoreImage 엔티티 생성
        // 리스트에 CDN public url 추가 -> DB 저장용
        // 변환된 파일명은 원본 파일명으로부터 extension을 분리한 후 .webp를 붙임
        String thumbFileName = thumbFile.getOriginalFilename().split("\\.(?=[^\\.]+$)")[0] + ".webp";
        thumbUrlList.add(publicThumbUrl1 + thumbFileName);
        thumbUrlList.add(publicThumbUrl2 + thumbFileName);

        for (MultipartFile mfile : mfiles) {
            contentUrlList.add(publicContentUrl + mfile.getOriginalFilename().split("\\.(?=[^\\.]+$)")[0] + ".webp");
        }
        
        // 썸네일 2장
        List<StoreImage> storeImageList = new ArrayList<>();
        storeImageList.add(StoreImage.builder()
                .store(store)
                .storeImageTag("MAIN_THUMBNAIL")
                .storeImageLink(thumbUrlList.get(0))
                .build()
        );
        storeImageList.add(StoreImage.builder()
                .store(store)
                .storeImageTag("SUB_THUMBNAIL")
                .storeImageLink(thumbUrlList.get(1))
                .build()
        );
        // 나머지 1 ~ 최대 5장 (대표로 지정된 이미지 포함)
        for (String url : contentUrlList) {
            storeImageList.add(StoreImage.builder()
                    .store(store)
                    .storeImageTag("CONTENT_DETAIL")
                    .storeImageLink(url)
                    .build()
            );
        }
        store.setStoreImageList(storeImageList); // setter 주입
        siRepo.saveAll(storeImageList);

        // 4. List<StoreCategory> 생성
        List<Category> categoryList = cRepo.findAllByCategoryIdList(dto.getCategoryList()); // 카테고리 엔티티 리스트
        List<StoreCategory> storeCategoryList = new ArrayList<>(); // 팝업스토어 - 카테고리 중계 테이블 엔티티 리스트
        for (Category category : categoryList) {
            storeCategoryList.add(StoreCategory.builder()
                    .store(store)
                    .category(category)
                    .build()
            );
        }
        store.setStoreCategoryList(storeCategoryList); // setter 주입
        scRepo.saveAll(storeCategoryList);

        // 5. ReservationEntry 생성
        List<ReservationEntry> reList = new ArrayList<>();
        LocalDate endDate = dto.getStoreEndDate();
        LocalTime endTime = dto.getStoreEndTime();

        int reserveGap = dto.getReserveGap();
        int capacity = dto.getCapacity();
        for (LocalDate curDate = dto.getStoreStartDate(); !curDate.isAfter(endDate); curDate = curDate.plusDays(1)) {
            for (LocalTime curTime = dto.getStoreStartTime(); curTime.isBefore(endTime); curTime = curTime.plusMinutes(reserveGap)) {
                reList.add(ReservationEntry.builder()
                        .store(store)
                        .entryDate(curDate)
                        .entryTime(curTime)
                        .reservedCount(0) // 이 부분 없애고 발생하는 에러 디버깅 필요 (@DynamicInsert, @DynamicUpdate 안먹히는 문제)
                        .capacity(capacity)
                        .entryStatus(ReservationEntry.EntryStatus.OPEN)
                        .build()
                );
            }
        }
        reRepo.saveAll(reList);

        // @Transactional 어노테이션으로 transaction.commit() 구문을 대체함.
        // sRepo.save(store);

        // 6. Elasticsearch 인덱스에 추가
        storeIndexService.save(store);

        // 7. 등록하려는 팝업스토어에 대한 운영자 계정 생성
        mRepo.save(Member.builder()
                .memberId("manager_" + String.format("%03d", store.getStoreId()))
                .memberPw(passwordEncoder.encode("Abcd123!"))
                .memberRole(Member.MemberRole.MANAGER)
                .memberName("운영자_" + String.format("%03d", store.getStoreId()))
                .store(store) // 팝업스토어 FK
                .build()
        );

        return ResponseEntity.ok().build();
    }
    
    
    /* [운영자] 예약 내역 목록 조회 */
    public ResponseEntity<?> getStoreReservationList(Long code) {
        Member member = null;
        try {
            member = mRepo.findByMemberCode(code).get();
        } catch (Exception e) {
            log.error("해당 code의 회원 조회 결과 없음.");
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        Store store = member.getStore();
        List<Reservation> reservationList = rRepo.findAllByStore_StoreId(store.getStoreId());

        // entity -> dto 변환
        List<StoreReservationResponseDto> dtoList = new ArrayList<>();
        for (Reservation reservation : reservationList) {
            StoreReservationResponseDto dto = StoreReservationResponseDto.builder()
                    .reservationId(reservation.getReservationId())
                    .reservedDate(reservation.getReservedDateTime().toLocalDate())
                    .reservedTime(reservation.getReservedDateTime().toLocalTime())
                    .memberName(reservation.getMember().getMemberName())
                    .memberPhone(reservation.getMember().getMemberPhone())
                    .reservationStatus(reservation.getReservationStatus().toString())
                    .build();

            dtoList.add(dto);
        }

        return ResponseEntity.ok(dtoList);
    }

    /* [운영자] 예약 현황 카운터 조회 (메트릭 영역) */
    public ResponseEntity<?> getStoreReservationCounter(Long code) {
        Member member = null;
        try {
            member = mRepo.findByMemberCode(code).get();
        } catch (Exception e) {
            log.error("해당 code의 회원 조회 결과 없음.");
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        Store store = member.getStore();
        Long confirmedCount = rRepo.countByStore_StoreIdAndReservationStatus(store.getStoreId(), Reservation.ReservationStatus.CONFIRMED);
        Long refusedCount = rRepo.countByStore_StoreIdAndReservationStatus(store.getStoreId(), Reservation.ReservationStatus.REFUSED);
        Long canceledCount = rRepo.countByStore_StoreIdAndReservationStatus(store.getStoreId(), Reservation.ReservationStatus.CANCELED);
        log.info("confirmed: {} | refused: {} | canceled: {}", confirmedCount, refusedCount, canceledCount);

        StoreReservationCounterResponseDto dto = StoreReservationCounterResponseDto.builder()
                .confirmed(confirmedCount)
                .refused(refusedCount)
                .canceled(canceledCount)
                .build();

        return ResponseEntity.ok(dto);
    }


    /* [운영자] 특정 예약에 대한 예약 상태 변경 로그 조회 */
    public ResponseEntity<?> getStoreReservationLog (Long rid) {
        List<ReservationLog> rlList = rlRepo.findAllByReservation_ReservationId(rid); // 추후 보완 필요 (WHERE 절에 store id도 들어가야 함.)
        List<ReservationLogResponseDto> dtoList = new ArrayList<>();
        // entity -> dto 변환s
        for (ReservationLog rl : rlList) {
            String method = rl.getReserveMethod().toString();
            // status까지 백 단에서 가공하기엔 너무 server에 부담을 줌.
            // 보안적 문제 없는 선에서 웬만하면 client가 처리하는게 나은 듯.
            String methodName = method.equals("V1") ? "직접승인" : "자동승인";
            ReservationLogResponseDto dto = ReservationLogResponseDto.builder()
                    .reservationLogId(rl.getReservationLogId())
                    .reservationId(rl.getReservation().getReservationId())
                    .reserveMethodCode(method.toLowerCase())
                    .reserveMethod(methodName)
                    .reservationStatus(rl.getReservationStatus().toString())
                    .timestamp(rl.getReservationStatusTimestamp())
                    .build();

            dtoList.add(dto);
        }

        return ResponseEntity.ok(dtoList);
    }
}
