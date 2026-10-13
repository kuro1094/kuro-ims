package com.kuro.ims.filter;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuro.ims.common.Result;
import com.kuro.ims.utils.JwtUtils;
import com.kuro.ims.utils.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.prefix}")
    private String prefix;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(header);
        if (StrUtil.isBlank(authHeader) || !authHeader.startsWith(prefix)) {
            chain.doFilter(request, response);
            return;
        }
        String token = authHeader.substring(prefix.length());
        Claims claims = jwtUtils.parseToken(token);
        if (claims == null || jwtUtils.isExpired(token)) {
            writeUnauthorized(response);
            return;
        }
        // 刷新 token 不能用于业务接口
        if ("refresh".equals(claims.get("type"))) {
            writeUnauthorized(response);
            return;
        }
        Long userId = claims.get("userId", Long.class);
        String username = claims.get("username", String.class);
        String role = claims.get("role", String.class);

        UserContext.set(userId, username, role);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                username, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            chain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(401, "登录已过期，请重新登录")));
    }
}
