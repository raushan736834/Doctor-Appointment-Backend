package com.harsh.AppointDoctor.Controllers;

import com.harsh.AppointDoctor.Services.PaymentService;
import com.razorpay.Order;
import com.razorpay.RazorpayException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @Getter
    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpaySecret;

    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> req) {
        try {
            Order order = paymentService.createOrder((Integer) req.get("amount"));
            Map<String, Object> res = Map.of(
                    "orderId", order.get("id"),
                    "amount", order.get("amount"),
                    "currency", order.get("currency"),
                    "key", paymentService.getRazorpayKeyId()
            );
            return ResponseEntity.ok(res);
        } catch (RazorpayException e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verify(@RequestBody Map<String, String> req) {
        boolean isValid = paymentService.verifySignature(
                req.get("razorpay_order_id"),
                req.get("razorpay_payment_id"),
                req.get("razorpay_signature")
        );

        return isValid
                ? ResponseEntity.ok(Map.of("status", "success"))
                : ResponseEntity.status(400).body(Map.of("status", "failure"));
    }

    @PostMapping("/refund")
    public ResponseEntity<?> refundPayment(@RequestBody Map<String, String> payload) {
        String payId = payload.get("payId");
        String auth = Base64.getEncoder().encodeToString((razorpayKeyId+":"+razorpaySecret).getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + auth);

        HttpEntity<String> entity = new HttpEntity<>("{}", headers);

        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.razorpay.com/v1/payments/" + payId + "/refund",
                    entity,
                    String.class
            );
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Refund failed: " + e.getMessage());
        }
    }

}

