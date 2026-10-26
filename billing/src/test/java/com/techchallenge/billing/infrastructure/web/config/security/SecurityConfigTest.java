package com.techchallenge.billing.infrastructure.web.config.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityConfigTest {

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig();
        ReflectionTestUtils.setField(
                securityConfig,
                "jwtSecret",
                "mySuperSecretKeyForJWTTokenGenerationThatIsSecureEnough"
        );
    }

    @Test
    void createsJwtDecoderWithConfiguredSecret() {
        JwtDecoder decoder = securityConfig.jwtDecoder();

        assertNotNull(decoder);
    }

    @Test
    void convertsRoleClaimToSpringAuthority() {
        JwtAuthenticationConverter converter = securityConfig.jwtAuthenticationConverter();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .claim("role", "admin")
                .build();

        var authentication = converter.convert(jwt);

        assertNotNull(authentication);
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())));
    }

    @Test
    void missingRoleClaimCreatesNoAuthorities() {
        JwtAuthenticationConverter converter = securityConfig.jwtAuthenticationConverter();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("subject")
                .build();

        var authentication = converter.convert(jwt);

        assertNotNull(authentication);
        assertFalse(authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().startsWith("ROLE_")));
    }
}
