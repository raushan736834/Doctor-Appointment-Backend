package com.harsh.AppointDoctor.config;

import com.harsh.AppointDoctor.Services.JWTService;
import com.harsh.AppointDoctor.Services.MyUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private final JWTService jwtService;

    @Autowired
    ApplicationContext context;

    @Autowired
    private final MyUserDetailsService myUserDetailsService;

    @Autowired
    public JwtFilter(JWTService jwtService, MyUserDetailsService myUserDetailsService) {
        this.jwtService = jwtService;
        this.myUserDetailsService = myUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")){
            token = authHeader.substring(7);
            try {
                email = jwtService.extractEmail(token);
            } catch (ExpiredJwtException ex) {
                // Log and explicitly set response status to 401
                logger.warn("Expired JWT token detected: {}");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Access token expired");
                return; // Stop filter chain, let frontend handle refresh
            } catch (Exception ex) {
                logger.error("JWT extraction error: {}");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid token");
                return; // Stop filter chain
            }
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDetails = context.getBean(MyUserDetailsService.class).loadUserByUsername(email);
            if (jwtService.validateToken(token, userDetails)){
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request,response);
    }
}