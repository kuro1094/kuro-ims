package com.kuro.ims.controller;

import com.kuro.ims.common.Result;
import com.kuro.ims.dto.LoginRequest;
import com.kuro.ims.dto.RegisterRequest;
import com.kuro.ims.dto.TokenResponse;
import com.kuro.ims.entity.User;
import com.kuro.ims.mapper.UserMapper;
import com.kuro.ims.service.AuthService;
import com.kuro.ims.utils.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;

    @PostMapping("/login")
    public Result<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/register")
    public Result<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(authService.register(request));
    }

    @PostMapping("/refresh")
    public Result<TokenResponse> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        return Result.success(authService.refresh(refreshToken));
    }

    @GetMapping("/profile")
    public Result<User> profile() {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}
