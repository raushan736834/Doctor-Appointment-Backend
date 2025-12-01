package com.harsh.AppointDoctor.Repo;

import com.harsh.AppointDoctor.Enums.RefundStatus;
import com.harsh.AppointDoctor.Models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    List<Payment> findByRefundStatus(RefundStatus status);
    Payment findByRefundId(String refundId);
    Payment findByPaymentId(String paymentId);

}
