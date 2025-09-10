package com.example.stock_predictor.service;

import com.example.stock_predictor.dto.MemoDTO;
import com.example.stock_predictor.exception.ResourceNotFoundException;
import com.example.stock_predictor.model.Memo;
import com.example.stock_predictor.model.Stock;
import com.example.stock_predictor.model.User;
import com.example.stock_predictor.repository.MemoRepository;
import com.example.stock_predictor.repository.StockRepository;
import com.example.stock_predictor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemoService {
    private final MemoRepository memoRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;

    public Memo createMemo(String userEmail, MemoDTO dto){
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Stock stock = stockRepository.findByTicker(dto.getTicker())
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));

        // 제목이 없으면 내용 앞 20자를 자동으로 제목으로 설정
        String finalTitle = (dto.getTitle() == null || dto.getTitle().trim().isEmpty())
                ? (dto.getContent().length() > 20 ? dto.getContent().substring(0, 20) + "..." : dto.getContent())
                : dto.getTitle();

        Memo memo = Memo.builder()
                .user(user)
                .stock(stock)
                .title(finalTitle)
                .content(dto.getContent())
                .stockDate(dto.getStockDate())
                .build();

        return memoRepository.save(memo);
    }

    public Page<Memo> getUserMemosByStock(Long stockId, Long userId, Pageable pageable){
        return memoRepository.findByStockIdAndUserIdOrderByCreatedAtDesc(stockId, userId ,pageable);
    }

    public Page<Memo> getMemoByUser(Long userId, Pageable pageable){
        return memoRepository.findByUserId(userId, pageable);
    }

    public Optional<Memo> getMemoById(Long id){
        return memoRepository.findById(id);
    }

    public Memo updateMemo(Long id, MemoDTO dto){
        Memo memo = memoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Memo not found"));

        String newTitle = (dto.getTitle() == null || dto.getTitle().trim().isEmpty())
                ? memo.getTitle()
                : dto.getTitle();

        LocalDateTime updatedAt = LocalDateTime.now();

        Memo updateMemo = Memo.builder()
                .id(memo.getId())
                .user(memo.getUser())
                .stock(memo.getStock())
                .title(newTitle)
                .content(dto.getContent())
                .stockDate(dto.getStockDate() != null ? dto.getStockDate() : memo.getStockDate())
                .createdAt(memo.getCreatedAt())
                .updatedAt(updatedAt)
                .build();

        return memoRepository.save(updateMemo);
    }

    public void deleteMemo(Long id){
        Memo memo = memoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Memo not found"));
        memoRepository.deleteById(id);
    }

    public Page<Memo> getMemoByUserAndStock(Long userId, String ticker, Pageable pageable){
        return memoRepository.findByUserIdAndStock_Ticker(userId, ticker, pageable);
    }

    public Long getUserIdByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }
}
