package com.harsh.AppointDoctor.Services;

import com.harsh.AppointDoctor.Utility.SignatureVerifier;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.Getter;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final RazorpayClient razorpayClient;

    @Getter
    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpaySecret;

    public PaymentService(RazorpayClient razorpayClient) {
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

}
