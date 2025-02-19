package com.harsh.AppointDoctor.Utility;

import lombok.*;

@Data
@NoArgsConstructor
public class LoginResponse {
    // Getters and Setters
    @Setter
    @Getter
    private String message;
    @Setter
    @Getter
    private int status;
    @Setter
    @Getter
    private String token;
    private String email;

    public LoginResponse(String message, int status, String token, String email) {
        this.message = message;
        this.status = status;
        this.token = token;
        this.email = email;
    }

}
