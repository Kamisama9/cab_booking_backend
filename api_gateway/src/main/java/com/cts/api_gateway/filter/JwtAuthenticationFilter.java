package com.cts.api_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            log.info("🔐 JWT Filter START - Method: {}, Path: {}",
                    request.getMethod(), request.getPath());

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.error("❌ JWT Filter FAILED - Missing or invalid Authorization header");
                log.error("   Authorization Header: {}", authHeader);
                return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            log.debug("🔑 Token (first 30 chars): {}...",
                    token.substring(0, Math.min(30, token.length())));

            try {
                Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

                log.debug("🔐 Parsing JWT token...");
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String userId = claims.getSubject();
                String role = claims.get("role", String.class);
                Date expiration = claims.getExpiration();
                Date now = new Date();

                log.info("📋 JWT Claims Extracted:");
                log.info("   User ID: {}", userId);
                log.info("   Role: {}", role);
                log.info("   Expires: {}", expiration);
                log.info("   Current Time: {}", now);
                log.info("   Is Expired? {}", expiration.before(now));

                if (expiration.before(now)) {
                    log.error("❌ JWT Filter FAILED - Token is EXPIRED");
                    return onError(exchange, "Token expired", HttpStatus.UNAUTHORIZED);
                }

                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", userId)
                        .header("X-User-Role", role)
                        .build();

                log.info("✅ JWT Filter SUCCESS - Forwarding with headers:");
                log.info("   X-User-Id: {}", userId);
                log.info("   X-User-Role: {}", role);

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                log.error("❌ JWT Filter FAILED - Token EXPIRED");
                log.error("   Expired at: {}", e.getClaims().getExpiration());
                return onError(exchange, "Token expired", HttpStatus.UNAUTHORIZED);

            } catch (io.jsonwebtoken.MalformedJwtException e) {
                log.error("❌ JWT Filter FAILED - Malformed token");
                log.error("   Error: {}", e.getMessage());
                return onError(exchange, "Invalid token format", HttpStatus.UNAUTHORIZED);

            } catch (io.jsonwebtoken.security.SignatureException e) {
                log.error("❌ JWT Filter FAILED - Invalid signature");
                log.error("   Error: {}", e.getMessage());
                return onError(exchange, "Invalid token signature", HttpStatus.UNAUTHORIZED);

            } catch (Exception e) {
                log.error("❌ JWT Filter FAILED - Unexpected error");
                log.error("   Exception Type: {}", e.getClass().getName());
                log.error("   Error Message: {}", e.getMessage());
                log.error("   Stack Trace: ", e);
                return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus status) {
        log.error("🚫 Returning error response: {} - {}", status, error);
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add("X-Error-Reason", error);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
    }
}