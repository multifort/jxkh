package com.iyunxin.jxkh.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * JWT 认证过滤器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // TODO: 注入 JwtUtil，暂时注释以避免循环依赖
    // private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (jwt != null) {
                // TODO: 实现 Token 验证逻辑
                // if (jwtUtil.validateToken(jwt)) {
                //     Long userId = jwtUtil.getUserIdFromToken(jwt);
                //     String username = jwtUtil.getUsernameFromToken(jwt);
                //     
                //     UsernamePasswordAuthenticationToken authentication = 
                //         new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
                //     authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                //     SecurityContextHolder.getContext().setAuthentication(authentication);
                //     
                //     // 将用户ID放入请求头，供Controller使用
                //     request.setAttribute("userId", userId);
                // }
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
