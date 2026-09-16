package github.sangwook.ecommerce.payment.infrastructure;

import github.sangwook.ecommerce.payment.PaymentStatus;
import github.sangwook.ecommerce.payment.application.PaymentRepository;
import github.sangwook.ecommerce.payment.domain.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public Payment save(Payment payment) {
        return paymentJpaRepository.save(payment);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return paymentJpaRepository.findById(id);
    }

    @Override
    public List<Payment> findAllByStatusAndNextCheckBefore(PaymentStatus status, OffsetDateTime time) {
        return paymentJpaRepository.findAllByPaymentStatusAndNextCheckAtBefore(status, time);
    }
}
