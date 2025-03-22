package com.ssginc.commonservice.backdoor.controller;

import com.ssginc.commonservice.backdoor.service.PerfTestReadService;
import com.ssginc.commonservice.util.PageResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * @author Queue-ri
 */

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/backdoor/test/read")
public class PerfTestReadRestController {

    private final PerfTestReadService testService;

    @GetMapping("/mysql")
    public Page readTestMySQL() {
        int pageIdx = 0; // 첫 페이지 조회
        return testService.getStoreList(pageIdx);
    }

    @GetMapping("/elasticsearch")
    public Page readTestElasticsearch() {
        int pageIdx = 0; // 첫 페이지 조회
        return testService.getStoreListIndex("", pageIdx);
    }

    @GetMapping("/redis")
    public Object readTestRedis() {
        int pageIdx = 0; // 첫 페이지 조회
        return testService.getStoreListCache("", pageIdx);
    }
}
