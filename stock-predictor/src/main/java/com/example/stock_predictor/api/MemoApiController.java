package com.example.stock_predictor.api;

import com.example.stock_predictor.dto.MemoDTO;
import com.example.stock_predictor.model.Memo;
import com.example.stock_predictor.service.MemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/memos")
public class MemoApiController {
    private final MemoService memoService;

    @PostMapping
    public ResponseEntity<MemoDTO> createMemo(@RequestBody MemoDTO dto,
                                           @AuthenticationPrincipal UserDetails userDetails){
        Memo savedMemo = memoService.createMemo(userDetails.getUsername(), dto);
        MemoDTO response = new MemoDTO(
                savedMemo.getStock().getTicker(),
                savedMemo.getContent(),
                savedMemo.getStockDate()
        );

        return ResponseEntity.ok(response);
    }
}
