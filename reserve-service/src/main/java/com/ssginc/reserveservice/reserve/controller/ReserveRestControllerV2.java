package com.ssginc.reserveservice.reserve.controller;

import com.ssginc.reserveservice.reserve.dto.ReserveRequestDto;
import com.ssginc.reserveservice.reserve.service.ReserveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Queue-ri
 */

@RequiredArgsConstructor
@RestController
@RequestMapping("/v2/reserve")
public class ReserveRestControllerV2 {
    private final ReserveService reserveService;

    @PostMapping
    public ResponseEntity<?> produceMessage(@RequestBody ReserveRequestDto dto) {
        return reserveService.sendMessage(dto);
    }
}
