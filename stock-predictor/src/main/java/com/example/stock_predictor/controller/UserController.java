package com.example.stock_predictor.controller;

import com.example.stock_predictor.dto.FavoriteDTO;
import com.example.stock_predictor.dto.StockWithPriceDTO;
import com.example.stock_predictor.dto.UserDTO;
import com.example.stock_predictor.model.Favorite;
import com.example.stock_predictor.model.StockPrice;
import com.example.stock_predictor.model.User;
import com.example.stock_predictor.repository.UserRepository;
import com.example.stock_predictor.service.FavoriteService;
import com.example.stock_predictor.service.StockPriceService;
import com.example.stock_predictor.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final FavoriteService favoriteService;
    private final AuthenticationManager authenticationManager;

    @GetMapping("/setting")
    public String setting(Model model, @AuthenticationPrincipal UserDetails userDetails){
        User user = userService.getUserByEmail(userDetails.getUsername());
        UserDTO dto = new UserDTO();
        dto.setUserName(user.getUserName());
        dto.setEmail(user.getEmail());

        model.addAttribute("user",dto);

        List<Favorite> favorites = favoriteService.getFavoritesLimited(userDetails.getUsername(), false);
        List<FavoriteDTO> favoriteDTO = favorites.stream()
                        .map(f -> new FavoriteDTO(f.getStock().getName(),f.getStock().getTicker()))
                                .collect(Collectors.toList());

        System.out.println(favoriteDTO);
        model.addAttribute("favorites", favoriteDTO);

        return "user-settings";
    }

    @PostMapping("/user/update")
    public String updateUser(@Valid UserDTO request,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes,
                             Model model){
        if (bindingResult.hasErrors()){
            // 오류가 있으면 다시 폼으로
            model.addAttribute("user", request);
            return "user-settings";
        }

        User user = userService.getUserByEmail(request.getEmail());
        userService.updateUser(request);

        redirectAttributes.addFlashAttribute("success", "회원 정보가 업데이트되었습니다.");
        return "redirect:/setting";
    }

    @PostMapping("/user/delete")
    public String deleteUserAccount(Authentication auth,
                                    HttpServletRequest request,
                                    HttpServletResponse response,
                                    RedirectAttributes redirectAttributes){
        User user = userService.getUserByEmail(auth.getName());
        userService.deleteUser(user.getEmail());

        SecurityContextHolder.clearContext();
        if (request.getSession(false) != null){
            request.getSession(false).invalidate();
        }

        redirectAttributes.addFlashAttribute("success", "회원탈퇴가 완료되었습니다.");
        return "redirect:/";
    }

    @GetMapping("/signup")
    public String signup(){
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid UserDTO request, HttpServletRequest httpRequest,
                         RedirectAttributes redirectAttributes
    ){
        try {
            User user = userService.signup(request);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword());

            Authentication auth = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(auth);

            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext()
            );

            return "redirect:/";
        } catch (IllegalArgumentException e){
            redirectAttributes.addFlashAttribute("errorMessage",e.getMessage());
            return "redirect:/signup";
        }
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response){
        new SecurityContextLogoutHandler().logout(request, response,
                SecurityContextHolder.getContext().getAuthentication());

        return  "redirect:/";
    }
}
