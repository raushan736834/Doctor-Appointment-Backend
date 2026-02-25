package com.harsh.AppointDoctor.Controllers;

import com.harsh.AppointDoctor.DTOs.ChatRequest;
import com.harsh.AppointDoctor.DTOs.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin("*")
public class ChatController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ai.agent.url:http://localhost:8000}")
    private String aiAgentUrl;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        try {
            // Get the currently authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated() &&
                    !"anonymousUser".equals(authentication.getPrincipal())) {

                // Extract user email from authentication
                Object principal = authentication.getPrincipal();
                if (principal instanceof UserDetails) {
                    UserDetails userDetails = (UserDetails) principal;
                    request.setUserId(userDetails.getUsername());
                }
            } else {
                // User is not logged in: set as guest
                request.setUserId("guest");
            }

            // Forward request to Python AI Agent
            String chatEndpoint = aiAgentUrl + "/chat";
            ResponseEntity<ChatResponse> response = restTemplate.postForEntity(
                    chatEndpoint,
                    request,
                    ChatResponse.class);

            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setResponse(
                    "I apologize, but I'm having trouble processing your request. Please try again later.");
            errorResponse.setIntent("ERROR");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
