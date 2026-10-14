package com.delicious.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRequest {
    /**
     * Optional refreshToken payload when sent via JSON body instead of Cookie.
     */
    private String refreshToken;
}
