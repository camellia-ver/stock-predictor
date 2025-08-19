package com.example.stock_predictor.controller;

import com.example.stock_predictor.dto.SignupRequest;
import com.example.stock_predictor.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/signup")
    public String signup(){
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(SignupRequest request){
        try {
            userService.signup(request);
            return "redirect:/login";
        } catch (IllegalArgumentException e){
            return "redirect:/signup?error=" + e.getMessage();
        }
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/setting")
    public String userSettings(){
        return "user-settings";
    }
}
