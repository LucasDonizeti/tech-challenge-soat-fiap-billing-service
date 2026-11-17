package com.techchallenge.billing.infrastructure.security;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ServiceJwtTokenProviderTest {

    private static final String JWT_SECRET = "mySuperSecretKeyForJWTTokenGenerationThatIsSecureEnough";

    @Test
    void createsShortLivedServiceTokenSignedWithSharedSecret() {
        String token = new ServiceJwtTokenProvider(JWT_SECRET).createToken();
        var claims = Jwts.parserBuilder()
                .setSigningKey(JWT_SECRET.getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals("billing-service", claims.getSubject());
        assertEquals("SERVICE", claims.get("role"));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }
}
