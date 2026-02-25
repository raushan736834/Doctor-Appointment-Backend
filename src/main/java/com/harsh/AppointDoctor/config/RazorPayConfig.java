package com.harsh.AppointDoctor.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class    RazorPayConfig {
    // In a configuration class
    @Bean
    public RazorpayClient razorpayClient(
            @Value("${razorpay.key.id}") String key,
            @Value("${razorpay.key.secret}") String secret) throws RazorpayException {
        return new RazorpayClient(key, secret);
    }
}
