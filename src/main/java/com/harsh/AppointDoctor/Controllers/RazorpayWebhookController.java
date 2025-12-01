package com.harsh.AppointDoctor.Controllers;

import com.harsh.AppointDoctor.Enums.RefundStatus;
import com.harsh.AppointDoctor.Models.Payment;
import com.harsh.AppointDoctor.Repo.AppointmentBookingRepo;
import com.harsh.AppointDoctor.Repo.PaymentRepository;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook/razorpay")
public class RazorpayWebhookController {

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    private final PaymentRepository paymentRepo;

    public RazorpayWebhookController(PaymentRepository paymentRepo,
                                     AppointmentBookingRepo appointmentRepo) {
        this.paymentRepo = paymentRepo;
    }

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestHeader("X-Razorpay-Signature") String signature,
            @RequestBody String payload) {

        // ✅ STEP 1: VERIFY SIGNATURE
        if (!verifySignature(payload, signature)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        }

        JSONObject event = new JSONObject(payload);

        String type = event.getString("event");

        switch (type) {

            case "refund.processed":
                handleRefundProcessed(event);
                break;

            case "refund.failed":
                handleRefundFailed(event);
                break;

            case "payment.captured":
                handlePaymentCaptured(event);
                break;
        }

        return ResponseEntity.ok("Webhook handled");
    }

    // ✅ VERIFY WEBHOOK SIGNATURE
    private boolean verifySignature(String payload, String signature) {
        try {
            return Utils.verifyWebhookSignature(payload, signature, webhookSecret);
        } catch (Exception e) {
            return false;
        }
    }

    // ✅ REFUND SUCCESS EVENT
    private void handleRefundProcessed(JSONObject event) {

        String refundId = event
                .getJSONObject("payload")
                .getJSONObject("refund")
                .getJSONObject("entity")
                .getString("id");

        Payment payment = paymentRepo.findByRefundId(refundId);
        payment.setRefundStatus(RefundStatus.COMPLETED);
        paymentRepo.save(payment);

    }

    // ✅ REFUND FAILURE EVENT
    private void handleRefundFailed(JSONObject event) {

        String refundId = event
                .getJSONObject("payload")
                .getJSONObject("refund")
                .getJSONObject("entity")
                .getString("id");

        Payment payment = paymentRepo.findByRefundId(refundId);
        payment.setRefundStatus(RefundStatus.FAILED);
        paymentRepo.save(payment);
    }

    // ✅ PAYMENT CAPTURE EVENT (OPTIONAL)
    private void handlePaymentCaptured(JSONObject event) {

        String paymentId = event
                .getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity")
                .getString("id");

        Payment payment = paymentRepo.findByPaymentId(paymentId);
        payment.setRefundStatus(RefundStatus.NOT_REQUIRED);
        paymentRepo.save(payment);
    }
}

