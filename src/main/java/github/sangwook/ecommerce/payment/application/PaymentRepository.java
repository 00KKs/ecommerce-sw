package github.sangwook.ecommerce.payment.application;

import github.sangwook.ecommerce.payment.domain.Payment;

import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findById(Long id);
}
