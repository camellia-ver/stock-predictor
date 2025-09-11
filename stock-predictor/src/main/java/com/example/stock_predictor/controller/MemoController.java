package com.example.stock_predictor.controller;

import com.example.stock_predictor.model.Memo;
import com.example.stock_predictor.model.enums.MemoSort;
import com.example.stock_predictor.model.User;
import com.example.stock_predictor.service.MemoService;
import com.example.stock_predictor.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class MemoController {
    private final MemoService memoService;
    private final UserService userService;

    @GetMapping("/memo")
    public String memo(@RequestParam(value = "page", defaultValue = "1") int page,
                       @RequestParam(value = "size", defaultValue = "6") int size,
                       @RequestParam(value = "sort", defaultValue = "latest") String sort,
                       @RequestParam(value = "stockTicker", required = false) String stockTicker,
                       @AuthenticationPrincipal UserDetails userDetails,
                       Model model){
        if (page < 1) page = 1;

        User user = userService.getUserByEmail(userDetails.getUsername());

        MemoSort memoSort = MemoSort.fromString(sort);
        PageRequest pageRequest = PageRequest.of(page - 1, size, memoSort.getSort());

        Page<Memo> memoPage = (stockTicker != null && !stockTicker.isEmpty())
                ? memoService.getMemoByUserAndStock(user.getId(), stockTicker, pageRequest)
                : memoService.getMemoByUser(user.getId(), pageRequest);

        model.addAttribute("memoList", memoPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", memoPage.getTotalPages());
        model.addAttribute("sort", sort);
        model.addAttribute("stockTicker", stockTicker);

        return "memo";
    }
}
