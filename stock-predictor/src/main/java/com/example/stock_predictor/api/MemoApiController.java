package com.example.stock_predictor.api;

import com.example.stock_predictor.dto.MemoDTO;
import com.example.stock_predictor.model.Memo;
import com.example.stock_predictor.service.MemoService;
import jakarta.validation.Valid;
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
    public ResponseEntity<MemoDTO> createMemo(@Valid @RequestBody MemoDTO dto,
                                           @AuthenticationPrincipal UserDetails userDetails){
        Memo savedMemo = memoService.createMemo(userDetails.getUsername(), dto);

        return ResponseEntity.ok(toDTO(savedMemo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemoDTO> getMemo(@PathVariable Long id){
        return memoService.getMemoById(id)
                .map(memo -> ResponseEntity.ok(toDTO(memo)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemoDTO> updateMemo(@PathVariable Long id,@Valid @RequestBody MemoDTO dto){
        Memo updated = memoService.updateMemo(id, dto);
        return ResponseEntity.ok(toDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMemo(@PathVariable Long id){
        memoService.deleteMemo(id);
        return ResponseEntity.noContent().build();
    }

    private MemoDTO toDTO(Memo memo){
        return new MemoDTO(
                memo.getStock().getTicker(),
                memo.getTitle(),
                memo.getContent(),
                memo.getStockDate()
        );
    }
}
