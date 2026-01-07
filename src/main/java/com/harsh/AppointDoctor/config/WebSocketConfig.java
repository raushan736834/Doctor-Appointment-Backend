package com.harsh.AppointDoctor.config;

import com.harsh.AppointDoctor.Services.JWTService;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Collections;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JWTService jwtUtil;

    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    @PostConstruct
    public void init() {
        log.info("=== WebSocket Configuration Initialized ===");
        log.info("STOMP endpoint: /ws");
        log.info("Allowed origins: {}", String.join(", ", allowedOrigins));
        log.info("Message broker prefixes: /topic, /queue");
        log.info("User destination prefix: /user");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for in-memory message routing
        config.enableSimpleBroker("/topic", "/queue");

        // Set application destination prefix
        config.setApplicationDestinationPrefixes("/app");

        // Set user destination prefix
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        log.info("Registering STOMP endpoint at /ws with SockJS");
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins)
                .withSockJS(); // Enable SockJS fallback
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                assert accessor != null;
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authToken = accessor.getFirstNativeHeader("Authorization");

                    if (authToken != null && authToken.startsWith("Bearer ")) {
                        String token = authToken.substring(7);

                        try {
                            // Extract email from token
                            String email = jwtUtil.extractEmail(token);
                            String tokenType = jwtUtil.extractTokenType(token);

                            // Validate token type and expiration
                            if ("access".equals(tokenType) && !jwtUtil.isTokenExpired(token)) {
                                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                        email,
                                        null,
                                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

                                accessor.setUser(authentication);
                                log.info("WebSocket authenticated for user: {}", email);
                            } else {
                                log.warn("WebSocket authentication failed: Invalid token type or token expired");
                            }
                        } catch (Exception e) {
                            log.error("WebSocket authentication failed", e);
                        }
                    } else {
                        log.warn("WebSocket CONNECT without Authorization header");
                    }
                }

                return message;
            }
        });
    }
}