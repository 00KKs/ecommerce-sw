package github.sangwook.ecommerce.payment.infrastructure;

import github.sangwook.ecommerce.payment.PaymentStatus;
import github.sangwook.ecommerce.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByPaymentStatusAndNextCheckAtBefore(PaymentStatus status, OffsetDateTime time);
}
