package com.example.stock_predictor.api;

import com.example.stock_predictor.dto.MemoDTO;
import com.example.stock_predictor.model.Memo;
import com.example.stock_predictor.service.MemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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
                savedMemo.getTitle(),
                savedMemo.getContent(),
                savedMemo.getStockDate()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<MemoDTO> getMemo(@PathVariable Long id){
        return memoService.getMemoById(id)
                .map(memo -> ResponseEntity.ok(new MemoDTO(
                        memo.getStock().getTicker(),
                        memo.getTitle(),
                        memo.getContent(),
                        memo.getStockDate())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemoDTO> updateMemo(@PathVariable Long id, @RequestBody MemoDTO dto){
        Memo updated = memoService.updateMemo(id, dto);
        return ResponseEntity.ok(new MemoDTO(updated.getStock().getTicker(),updated.getTitle(), updated.getContent(), updated.getStockDate()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMemo(@PathVariable Long id){
        memoService.deleteMemo(id);
        return ResponseEntity.noContent().build();
    }
}
