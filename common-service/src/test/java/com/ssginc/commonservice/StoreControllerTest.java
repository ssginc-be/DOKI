package com.ssginc.commonservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssginc.commonservice.store.controller.StoreController;
import com.ssginc.commonservice.store.document.StoreMetaDocument;
import com.ssginc.commonservice.store.dto.StoreSaveRequestDto;
import com.ssginc.commonservice.store.model.*;
import com.ssginc.commonservice.store.service.StoreIndexService;
import com.ssginc.commonservice.store.service.StoreService;
import com.ssginc.commonservice.util.PageResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Queue-ri
 */

@SpringBootTest
@AutoConfigureMockMvc
public class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Mock
    private StoreService storeService;  // StoreService 모킹: 서비스 레이어를 테스트에서 제외

    @Autowired
    private StoreIndexService storeIndexService;

    @Autowired
    private ElasticsearchOperations esOp;

    // @InjectMocks: StoreController에 모킹된 storeService를 주입
    //@InjectMocks
    //private StoreController storeController;

    @Autowired
    private StoreRepository sRepo;

    @Autowired
    private CategoryRepository cRepo;

    @BeforeEach
    public void setUp() {
        // 여기에 필요한 설정이나 Mock 초기화 로직 추가

    }

    @DisplayName("팝업스토어 등록 테스트")
    @Test
    public void testRegisterStore() throws Exception {

        // 테스트 등록 요청 데이터
        StoreSaveRequestDto storeSaveRequestDto = StoreSaveRequestDto.builder()
                .storeName("리락쿠마 콜라보 카페 in 서울")
                .categoryList(Arrays.asList(4L, 8L))  // 카테고리 리스트
                .storeBranch("신세계백화점 강남점")  // 지점명
                .storeAt("1F 오픈스테이지")  // 장소
                .storeShortDesc("국내 최초 리락쿠마 카페 OPEN!")  // 짧은 설명
                .storeLongDesc("[ 리락쿠마 X DOKI ]\n\n✨국내 최초✨리락쿠마 카페 OPEN🎉")  // 상세 설명
                .storeStartDate(LocalDate.of(2025, 3, 25))  // 시작일
                .storeEndDate(LocalDate.of(2025, 3, 27))  // 종료일
                .storeStartTime(LocalTime.of(12, 0))  // 시작시간
                .storeEndTime(LocalTime.of(14, 0))  // 종료시간
                .reserveMethod("V1")
                .reserveGap(30)  // 예약 간격
                .capacity(20)  // 예약 정원
                .thumbIdx(0)  // 썸네일 인덱스 (첫 번째 이미지를 썸네일로 설정)
                .build();

        // 테스트 데이터를 multipart json에 첨부
        String jsonRequest = objectMapper.writeValueAsString(storeSaveRequestDto);
        MockMultipartFile jsonFile = new MockMultipartFile(
                "json",  // 파라미터 이름
                "storeSaveRequestDto.json",  // 파일 이름 (확장자 .json)
                "application/json",  // MIME 타입
                jsonRequest.getBytes()  // JSON 문자열을 byte[]로 변환하여 첨부
        );

        // 테스트 이미지를 multipart image에 첨부
        File file = new File(getClass().getClassLoader().getResource("store_test_thumb.png").toURI());
        FileInputStream fileInputStream = new FileInputStream(file);
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                file.getName(),  // 파일 이름
                "image/png",  // MIME 타입
                fileInputStream
        );

        // StoreService의 registerStore() 정상 반환 체크
        when(storeService.registerStore(Mockito.any(StoreSaveRequestDto.class), Mockito.anyList()))
                .thenReturn(ResponseEntity.ok().build());

        // StoreRestControllerV1 내 API 검증
        mockMvc.perform(MockMvcRequestBuilders.multipart("/v1/store/registration")
                        .file(jsonFile)
                        .file(imageFile)
                        .contentType("multipart/form-data"))
                .andExpect(status().isOk()); // 200 OK 검증
    }

    @DisplayName("팝업스토어 검색 테스트")
    @Test
    @Transactional
    public void testStoreSearch() throws Exception {

        // 테스트 등록 요청 데이터
        Store storeTestEntity = Store.builder()
                .storeName("[테스트] 리락쿠마 콜라보 카페 in 서울")
                .storeBranch("신세계백화점 강남점")  // 지점명
                .storeAt("1F 오픈스테이지")  // 장소
                .storeShortDesc("국내 최초 리락쿠마 카페 OPEN!")  // 짧은 설명
                .storeLongDesc("[ 리락쿠마 X DOKI ]\n\n✨국내 최초✨리락쿠마 카페 OPEN🎉")  // 상세 설명
                .storeStartDate(LocalDate.of(2025, 3, 25))  // 시작일
                .storeEndDate(LocalDate.of(2025, 3, 27))  // 종료일
                .storeStartTime(LocalTime.of(12, 0))  // 시작시간
                .storeEndTime(LocalTime.of(14, 0))  // 종료시간
                .storeReserveMethod(Store.StoreReserveMethod.V1)
                .storeReserveGap(30)  // 예약 간격
                .storeCapacity(20)  // 예약 정원
                .storeStatus(Store.StoreStatus.ACTIVE)
                .build();

        // StoreCategory 중계테이블 설정 - ES 인덱싱을 위해 필요
        List<StoreCategory> scList = new ArrayList<>();
        Category category1 = cRepo.findById(4L).get();
        Category category2 = cRepo.findById(8L).get();
        scList.add(StoreCategory.builder()
                .store(storeTestEntity)
                .category(category1)
                .build()
        );
        scList.add(StoreCategory.builder()
                .store(storeTestEntity)
                .category(category2)
                .build()
        );
        storeTestEntity.setStoreCategoryList(scList);
        
        // StoreImage 설정
        List<StoreImage> imageList = new ArrayList<>();
        imageList.add(StoreImage.builder()
                .store(storeTestEntity)
                .storeImageTag("MAIN_THUMBNAIL")
                .storeImageLink("https://contents.creators.mypetlife.co.kr/content/uploads/2020/10/08142646/0a951790cfdb4f93bd36c6dc53862c52-384x384.png")
                .build()
        );
        imageList.add(StoreImage.builder()
                .store(storeTestEntity)
                .storeImageTag("SUB_THUMBNAIL")
                .storeImageLink("https://contents.creators.mypetlife.co.kr/content/uploads/2020/10/08142646/0a951790cfdb4f93bd36c6dc53862c52-384x384.png")
                .build()
        );
        imageList.add(StoreImage.builder()
                .store(storeTestEntity)
                .storeImageTag("CONTENT_DETAIL")
                .storeImageLink("https://contents.creators.mypetlife.co.kr/content/uploads/2020/10/08142646/0a951790cfdb4f93bd36c6dc53862c52-384x384.png")
                .build()
        );
        storeTestEntity.setStoreImageList(imageList);
        
        // 테스트 엔티티 적재
        sRepo.save(storeTestEntity); // DB

        // store 적재 여부 검증
        Optional<Store> storeOpt = sRepo.findByStoreName("[테스트] 리락쿠마 콜라보 카페 in 서울");
        assertTrue("팝업스토어가 등록되지 않음.", storeOpt.isPresent());

        // 적재 데이터 무결성 검증 (등록된 데이터가 예상한 대로 들어갔는지 확인)
        Store store = storeOpt.get();
        assertEquals("등록된 팝업스토어의 이름이 불일치함.", "[테스트] 리락쿠마 콜라보 카페 in 서울", store.getStoreName());
        assertEquals("등록된 팝업스토어의 지점이 불일치함.", "신세계백화점 강남점", store.getStoreBranch());
        assertEquals("등록된 팝업스토어의 시작일이 불일치함.", LocalDate.of(2025, 3, 25), store.getStoreStartDate());
        assertEquals("등록된 팝업스토어의 종료일이 불일치함.", LocalDate.of(2025, 3, 27), store.getStoreEndDate());
        assertEquals("등록된 팝업스토어의 예약 정원이 불일치함.", 20, store.getStoreCapacity());

        // 테스트 데이터 인덱싱
        storeIndexService.save(storeTestEntity); // ES
        
        // 인덱싱한 팝업스토어가 검색되는지 확인
        mockMvc.perform(MockMvcRequestBuilders.get("/search")
                        .param("q", "테스트"))
                .andExpect(status().isOk())
                .andExpect(view().name("store/store_search_result")) // 뷰 이름이 맞는지 확인
                .andExpect(model().attributeExists("storeList")); // 모델에 storeList가 있는지 확인
//                .andExpect(model().attribute("storeList", hasItem(
//                        hasProperty("storeName", is("[테스트] 리락쿠마 콜라보 카페 in 서울"))
//                ))); // 검색 결과의 storeName 확인

        Thread.sleep(500);  // 1초 대기

        PageResponseDto page = storeIndexService.getStoreListInternal("테스트", 0); // v2 팝업스토어 조회
        List<StoreMetaDocument> storeList = (List<StoreMetaDocument>) page.getData(); // downcast
        assertTrue("테스트 데이터가 검색되지 않음.", !storeList.isEmpty());
    }
}
