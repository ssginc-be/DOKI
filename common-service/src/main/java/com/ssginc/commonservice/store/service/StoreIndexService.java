package com.ssginc.commonservice.store.service;

import com.ssginc.commonservice.store.document.StoreMetaDocument;
import com.ssginc.commonservice.store.document.StoreMetaDocumentRepository;
import com.ssginc.commonservice.store.model.Store;
import com.ssginc.commonservice.store.model.StoreRepository;
import com.ssginc.commonservice.util.PageResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Queue-ri
 */

@Slf4j
@RequiredArgsConstructor
@Service
public class StoreIndexService {
    /*
        팝업스토어 관련 Elasticsearch 인덱싱을 위한 내부 service - 일괄 삭제는 여기서 담당 x
    */
    private final ElasticsearchOperations esOp;
    private final StoreRepository sRepo; // DB
    private final StoreMetaDocumentRepository smdRepo; // Elasticsearch


    /* 팝업스토어 관련 인덱싱 */
    public void save(Store store) {
        // 팝업스토어 메타 데이터 인덱싱 - 목록 조회
        esOp.save(StoreMetaDocument.from(store));
        
        // 팝업스토어 상세 데이터 인덱싱 - 상세 조회
        // esOp.save(StoreInfoDocument.from(store));
    }

    /* 팝업스토어 관련 인덱스 삭제 */
    public void delete(Store store) {
        // 팝업스토어 메타 데이터 인덱싱 - 목록 조회
        esOp.delete(StoreMetaDocument.from(store));

        // 팝업스토어 상세 데이터 인덱싱 - 상세 조회
        // esOp.save(StoreInfoDocument.from(store));
    }


    @Transactional // 빼면 Lazy fetch 관련 에러 터짐
    @EventListener(ApplicationReadyEvent.class)
    /* 모든 팝업스토어 데이터를 DB로부터 가져와서 인덱싱 - 스프링 시작시 실행할 용도, 외부 API 없음 */
    public void cleanAndIndexAllStore() {
        // ddl-auto: create와 같은 방식이라고 생각하면 됨

        // 이전 인덱스 삭제
        log.info("[Store] Elasticsearch 인덱스 삭제 시작");
        List<StoreMetaDocument> metaList = smdRepo.findAll(); // 전체 팝업스토어 메타 데이터 인덱스
        for (StoreMetaDocument doc : metaList) {
            esOp.delete(doc);
        }
        log.info("[Store] Elasticsearch 인덱스 삭제 완료");

        // 새 인덱스 생성
        log.info("[Store] Elasticsearch 인덱싱 시작");
        List<Store> storeList = sRepo.findAll();
        for (Store store : storeList) {
            save(store);
        }
        log.info("[Store] Elasticsearch 인덱싱 완료");
    }

    /* 팝업스토어 메타 데이터 인덱스 검색 */
    public ResponseEntity<?> getStoreListByKeyword(Integer pageIdx, String keyword) {
        // 페이징 크기
        final int FETCH_SIZE = 9;

        // PageRequest 객체 생성
        if (pageIdx > 0) pageIdx -= 1; // 사용자의 1페이지 == 서버의 0페이지
        PageRequest pageRequest = PageRequest.of(pageIdx, FETCH_SIZE, Sort.by("storeStartDate").descending());

        // 인덱스에서 조회
        Page<StoreMetaDocument> docPage = smdRepo.findByStoreNameContainsIgnoreCase(keyword, pageRequest);

        // 반환할 page 객체 작성
        PageResponseDto<?> page = PageResponseDto.builder()
                .data(docPage.getContent())
                .first(docPage.isFirst())
                .last(docPage.isLast())
                .empty(docPage.isEmpty())
                .totalElements(docPage.getTotalElements())
                .totalPages(docPage.getTotalPages())
                .numberOfElements(docPage.getNumberOfElements())
                .pageSize(docPage.getSize())
                .pageNumber(docPage.getNumber())
                .build();

        return ResponseEntity.ok().body(page);
    }

    /* 팝업스토어 메타 데이터 인덱스 조회 - 내부에서만 사용 */
    public PageResponseDto getStoreListInternal(String keyword, Integer pageIdx) {
        // 페이징 크기
        final int FETCH_SIZE = 9;

        // PageRequest 객체 생성
        if (pageIdx > 0) pageIdx -= 1; // 사용자의 1페이지 == 서버의 0페이지
        PageRequest pageRequest = PageRequest.of(pageIdx, FETCH_SIZE, Sort.by("storeStartDate").descending());

        // 인덱스에서 조회 - 검색키워드 없음
        Page<StoreMetaDocument> docPage = smdRepo.findByStoreNameContainsIgnoreCase(keyword, pageRequest);

        // 반환할 page 객체 작성
        PageResponseDto<?> page = PageResponseDto.builder()
                .data(docPage.getContent())
                .first(docPage.isFirst())
                .last(docPage.isLast())
                .empty(docPage.isEmpty())
                .totalElements(docPage.getTotalElements())
                .totalPages(docPage.getTotalPages())
                .numberOfElements(docPage.getNumberOfElements())
                .pageSize(docPage.getSize())
                .pageNumber(docPage.getNumber())
                .build();

        return page;
    }
}
