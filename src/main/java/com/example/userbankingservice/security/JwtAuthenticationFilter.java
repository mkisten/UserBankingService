package com.example.userbankingservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final String jwtSecret;
    private final UserDetailsService userDetailsService;
    private final SecretKey signingKey;

    public JwtAuthenticationFilter(@Value("${jwt.secret}") String jwtSecret, UserDetailsService userDetailsService) {
        this.jwtSecret = jwtSecret;
        this.userDetailsService = userDetailsService;

        // Создаем SecretKey из строки секрета
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);

        // Убеждаемся что ключ достаточно длинный для HMAC-SHA256 (минимум 32 байта)
        if (keyBytes.length < 32) {
            // Если ключ короткий, дополняем его до 32 байт
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 32));
            keyBytes = paddedKey;
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        logger.debug("Authorization header: {}", header);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            logger.debug("Extracted token: {}", token);

            try {
                Claims claims = Jwts.parser()
                        .verifyWith(signingKey)
                        .build()
                        .parseClaimsJws(token)
                        .getPayload();

                Long userId = claims.get("userId", Long.class);
                logger.debug("Extracted userId from token: {}", userId);

                if (userId != null) {
                    // Создаем аутентификацию с userId как Principal
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userId,
                                    null,
                                    AuthorityUtils.createAuthorityList("ROLE_USER")
                            );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("Успешная аутентификация пользователя ID: {}", userId);
                } else {
                    logger.warn("UserId не найден в JWT токене");
                    SecurityContextHolder.clearContext();
                }
            } catch (JwtException e) {
                logger.error("Недействительный JWT токен: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            } catch (Exception e) {
                logger.error("Ошибка проверки JWT: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        } else {
            logger.debug("No Authorization header or not a Bearer token");
        }

        chain.doFilter(request, response);
    }
}