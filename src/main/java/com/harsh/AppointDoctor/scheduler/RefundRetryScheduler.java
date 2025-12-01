package com.harsh.AppointDoctor.scheduler;

import com.harsh.AppointDoctor.Enums.RefundStatus;
import com.harsh.AppointDoctor.Models.Payment;
import com.harsh.AppointDoctor.Repo.PaymentRepository;
import com.harsh.AppointDoctor.Services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundRetryScheduler {

    private final PaymentRepository paymentRepo;
    private final PaymentService paymentService;

    @Value("${refund.retry.max}")
    private int MAX_RETRY;

    @Value("${refund.retry.interval.minutes}")
    private int INTERVAL_MIN;

    // ✅ Runs every 10 minutes (or what you configured)
    @Scheduled(fixedRateString = "#{${refund.retry.interval.minutes} * 60 * 6000}")
    public void retryFailedRefunds() {

        log.info("Refund Retry Scheduler started...");

        List<Payment> failedRefunds = paymentRepo.findByRefundStatus(RefundStatus.FAILED);

        for (Payment payment : failedRefunds) {

            if (payment.getRefundRetryCount() >= MAX_RETRY) {
                log.warn("Refund retry limit exceeded for paymentId={}", payment.getPaymentId());
                continue;
            }

            try {
                log.info("Retrying refund for paymentId={}", payment.getPaymentId());

                boolean success = paymentService.initiateRefund(payment);

                if (success) {
                    log.info("Refund retry success for paymentId={}", payment.getPaymentId());
                    continue;
                }

            } catch (Exception e) {
                log.error("Retry refund error for paymentId={}", payment.getPaymentId(), e);
            }

            // ✅ Increment attempt
            payment.setRefundRetryCount(payment.getRefundRetryCount() + 1);
            payment.setLastRetryAt(LocalDateTime.now());
            paymentRepo.save(payment);
        }
    }
}

