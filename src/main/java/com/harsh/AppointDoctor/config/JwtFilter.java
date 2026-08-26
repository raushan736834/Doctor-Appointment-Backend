package com.harsh.AppointDoctor.config;

import com.harsh.AppointDoctor.Services.JWTService;
import com.harsh.AppointDoctor.Services.MyUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final MyUserDetailsService myUserDetailsService;

    @Autowired
    ApplicationContext context;

    @Autowired
    public JwtFilter(JWTService jwtService, MyUserDetailsService myUserDetailsService) {
        this.jwtService = jwtService;
        this.myUserDetailsService = myUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Skip JWT validation for public endpoints
        if (path.startsWith("/api/public") ||
                path.startsWith("/auth") ||
                path.startsWith("/ws")

        ) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = null;
        String email = null;

        // 1. Try from Authorization header (priority for access tokens)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        // 2. If header missing, try from cookie (fallback)
        if (token == null && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // 3. Validate and extract email from access token
        if (token != null) {
            try {
                // Ensure it's an access token, not refresh token
                String tokenType = jwtService.extractTokenType(token);
                if (!"access".equals(tokenType)) {
                    logger.warn("Non-access token provided in Authorization header");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Invalid token type\"}");
                    return;
                }

                email = jwtService.extractEmail(token);
            } catch (ExpiredJwtException ex) {
                logger.warn("Expired access token detected");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Access token expired\", \"code\": \"TOKEN_EXPIRED\"}");
                return;
            } catch (Exception ex) {
                logger.error("JWT extraction error: " + ex.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Invalid token\"}");
                return;
            }
        }

        // 4. If email found and not already authenticated, set Authentication
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = context.getBean(MyUserDetailsService.class).loadUserByUsername(email);
                if (jwtService.validateAccessToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception ex) {
                logger.error("Authentication setup error: " + ex.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Authentication failed\"}");
                return;
            }
        }

        // 5. Continue filter chain
        filterChain.doFilter(request, response);
    }
}