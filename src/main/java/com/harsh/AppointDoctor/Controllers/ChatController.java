package com.harsh.AppointDoctor.Controllers;

import com.harsh.AppointDoctor.DTOs.ChatRequest;
import com.harsh.AppointDoctor.DTOs.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin("*")
public class ChatController {

//    @Autowired
//    private RestTemplate restTemplate;
//
//    // Ensure this matches the Python service port
//    private static final String AI_AGENT_URL = "http://localhost:8000/chat";
//
//    @PostMapping
//    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
//        try {
//            // Get the currently authenticated user
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//            if (authentication != null && authentication.isAuthenticated() &&
//                    !"anonymousUser".equals(authentication.getPrincipal())) {
//
//                // Assuming the Principal is our User entity (as returned by
//                // CustomUserDetailService)
//                // If using JWT, this should be set by the filter.
//                Object principal = authentication.getPrincipal();
//                if (principal instanceof User) {
//                    User user = (User) principal;
////                    request.setUserId(String.valueOf(user.getId()));
//                }
//            } else {
//                // User is not logged in: set as guest or handle accordingly
//                // We overwrite to ensure frontend doesn't spoof ID
//                request.setUserId("guest");
//            }
//
//            // Forward request to Python AI Agent
//            ResponseEntity<ChatResponse> response = restTemplate.postForEntity(AI_AGENT_URL, request,
//                    ChatResponse.class);
//            return ResponseEntity.ok(response.getBody());
//        } catch (Exception e) {
//            e.printStackTrace();
//            ChatResponse errorResponse = new ChatResponse();
//            errorResponse.setResponse("Error communicating with AI service: " + e.getMessage());
//            return ResponseEntity.internalServerError().body(errorResponse);
//        }
//    }
}
