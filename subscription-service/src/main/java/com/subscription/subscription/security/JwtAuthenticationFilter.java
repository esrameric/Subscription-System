package com.subscription.subscription.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT token'ı doğrulayan ve SecurityContext'e authentication bilgisi ekleyen filter.
 * Her HTTP request'i için bir kez çalışır.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        
        // Authorization header yoksa veya Bearer ile başlamıyorsa, devam et
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Token'ı çıkar (Bearer prefix'ini kaldır)
        final String token = header.substring(7);
        
        // Token geçersizse, devam et (authentication olmadan)
        if (!jwtUtil.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Token geçerliyse, username'i al ve SecurityContext'e ekle
        String username = jwtUtil.getUsernameFromToken(token);
        
        // Authentication nesnesi oluştur
        // Not: subscription-service'te UserDetailsService yok, 
        // sadece token'dan gelen bilgiyi kullanıyoruz
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                username, 
                null, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        
        // SecurityContext'e authentication'ı set et
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }
}
