package com.kuro.ims.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kuro.ims.common.BizException;
import com.kuro.ims.dto.LoginRequest;
import com.kuro.ims.dto.RegisterRequest;
import com.kuro.ims.dto.TokenResponse;
import com.kuro.ims.entity.User;
import com.kuro.ims.mapper.UserMapper;
import com.kuro.ims.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public TokenResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (user == null) {
            throw new BizException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException("账号已被禁用，请联系管理员");
        }
        return buildTokenResponse(user);
    }

    public TokenResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BizException("两次输入的密码不一致");
        }
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BizException("用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() == null ? request.getUsername() : request.getNickname());
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);
        return buildTokenResponse(user);
    }

    public TokenResponse refresh(String refreshToken) {
        Claims claims = jwtUtils.parseToken(refreshToken);
        if (claims == null || jwtUtils.isExpired(refreshToken)) {
            throw new BizException(401, "refresh token 已过期，请重新登录");
        }
        if (!"refresh".equals(claims.get("type"))) {
            throw new BizException(401, "非法的 refresh token");
        }
        Long userId = claims.get("userId", Long.class);
        User user = userMapper.selectById(userId);
        if (user == null || (user.getStatus() != null && user.getStatus() == 0)) {
            throw new BizException(401, "用户不存在或已被禁用");
        }
        return buildTokenResponse(user);
    }

    private TokenResponse buildTokenResponse(User user) {
        String accessToken = jwtUtils.createToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtils.createRefreshToken(user.getId(), user.getUsername(), user.getRole());
        TokenResponse.UserInfo userInfo = new TokenResponse.UserInfo(
                user.getId(), user.getUsername(), user.getNickname(), user.getAvatar(), user.getRole());
        return new TokenResponse(accessToken, refreshToken, jwtUtils.getExpire() / 1000, userInfo);
    }
}
