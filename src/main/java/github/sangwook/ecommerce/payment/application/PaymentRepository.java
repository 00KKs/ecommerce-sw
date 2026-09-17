package github.sangwook.ecommerce.payment.application;

import github.sangwook.ecommerce.payment.PaymentStatus;
import github.sangwook.ecommerce.payment.domain.Payment;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findById(Long id);

    List<Payment> findAllByStatusAndNextCheckBefore(PaymentStatus status, OffsetDateTime time);
}
