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
}
