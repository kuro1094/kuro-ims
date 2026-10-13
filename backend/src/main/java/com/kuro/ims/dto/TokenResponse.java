package com.kuro.ims.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenResponse {

    private String accessToken;

    private String refreshToken;

    /** access token 有效期（秒） */
    private Long expiresIn;

    private UserInfo user;

    @Data
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
        private String role;
    }
}
