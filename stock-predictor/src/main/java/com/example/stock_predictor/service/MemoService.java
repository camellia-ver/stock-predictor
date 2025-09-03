package com.example.stock_predictor.service;

import com.example.stock_predictor.dto.MemoDTO;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemoService {
    private final MemoRepository memoRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;

    public Memo createMemo(String userEmail, MemoDTO dto){
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Stock stock = stockRepository.findByTicker(dto.getTicker())
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        Memo memo = Memo.builder()
                .user(user)
                .stock(stock)
                .content(dto.getContent())
                .stockDate(dto.getStockDate())
                .build();

        return memoRepository.save(memo);
    }

    public Page<Memo> getUserMemosByStock(Long stockId, Long userId, Pageable pageable){
        return memoRepository.findByStockIdAndUserIdOrderByCreatedAtDesc(stockId, userId ,pageable);
    }
}
