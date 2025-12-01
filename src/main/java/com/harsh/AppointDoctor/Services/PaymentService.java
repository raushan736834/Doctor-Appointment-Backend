package com.harsh.AppointDoctor.Services;

import com.harsh.AppointDoctor.Enums.RefundStatus;
import com.harsh.AppointDoctor.Models.Payment;
import com.harsh.AppointDoctor.Repo.PaymentRepository;
import com.harsh.AppointDoctor.Utility.SignatureVerifier;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.Getter;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final RazorpayClient razorpayClient;

    @Getter
    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpaySecret;

    public PaymentService(PaymentRepository paymentRepo, RazorpayClient razorpayClient) {
        this.paymentRepo = paymentRepo;
        this.razorpayClient = razorpayClient;
    }

    public Order createOrder(int amountInPaise) throws RazorpayException {
        JSONObject opts = new JSONObject();
        opts.put("amount", amountInPaise);
        opts.put("currency", "INR");
        opts.put("receipt", "order_rcpt_" + System.currentTimeMillis());
        opts.put("payment_capture", 1);
        return razorpayClient.orders.create(opts);
    }

    public boolean verifySignature(String orderId, String paymentId, String signature) {
        String payload = orderId + "|" + paymentId;
        return SignatureVerifier.verify(payload, signature, razorpaySecret);
    }

    public boolean initiateRefund(Payment payment) {
        try {
            String auth = Base64.getEncoder()
                    .encodeToString((razorpayKeyId + ":" + razorpaySecret).getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + auth);

            HttpEntity<String> entity = new HttpEntity<>("{}", headers);

            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.razorpay.com/v1/payments/" + payment.getPaymentId() + "/refund",
                    entity,
                    String.class
            );

            payment.setRefundStatus(RefundStatus.INITIATED);
            paymentRepo.save(payment);
            return true;

        } catch (Exception e) {
            payment.setRefundStatus(RefundStatus.FAILED);
            paymentRepo.save(payment);
            return false;
        }
    }

}
