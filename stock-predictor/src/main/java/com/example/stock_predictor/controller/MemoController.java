package com.example.stock_predictor.controller;

import com.example.stock_predictor.model.Memo;
import com.example.stock_predictor.model.User;
import com.example.stock_predictor.repository.UserRepository;
import com.example.stock_predictor.service.MemoService;
import com.example.stock_predictor.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

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
        User user = userService.getUserByEmail(userDetails.getUsername());

        Sort sorting;
        switch (sort) {
            case "oldest":
                sorting = Sort.by(Sort.Direction.ASC, "createdAt");
                break;
            case "title":
                sorting = Sort.by(Sort.Direction.ASC, "title");
                break;
            case "latest":
            default:
                sorting = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        PageRequest pageRequest = PageRequest.of(page - 1, size, sorting);
        Page<Memo> memoPage;

        if (stockTicker != null){
            memoPage = memoService.getMemoByUserAndStock(user.getId(), stockTicker, pageRequest);
        }else {
            memoPage = memoService.getMemoByUser(user.getId(), pageRequest);
        }

        model.addAttribute("memoList", memoPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", memoPage.getTotalPages());
        model.addAttribute("sort", sort);
        model.addAttribute("stockTicker", stockTicker);

        return "memo";
    }
}
