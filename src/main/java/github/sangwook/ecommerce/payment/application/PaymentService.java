package github.sangwook.ecommerce.payment.application;

import github.sangwook.ecommerce.payment.domain.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentLookupRetryPolicy paymentLookupRetryPolicy;

    @Transactional
    public Long ready(Long orderId, int amount, String paymentKey, UUID idempotencyKey) {
        Payment payment = new Payment(orderId, amount, paymentKey, idempotencyKey);
        payment = paymentRepository.save(payment);
        return payment.getId();
    }

    @Transactional
    public void success(Long paymentId) {
        Payment payment = getById(paymentId);
        payment.approve();
        paymentRepository.save(payment);
    }

    @Transactional
    public void aborted(Long paymentId) {
        Payment payment = getById(paymentId);
        payment.aborted();
        paymentRepository.save(payment);
    }

    @Transactional
    public void unknown(Long paymentId) {
        Payment payment = getById(paymentId);
        payment.markAsUnknown();
        if (payment.hasExceededRetryLimit(paymentLookupRetryPolicy.maxRetryCount())) {
            payment.marksAsRequiresReview();
        } else {
            Duration delay = Duration.ofSeconds(paymentLookupRetryPolicy.nextDelaySeconds(payment.getCheckCount() + 1));
            payment.scheduleNextCheck(delay);
        }

        paymentRepository.save(payment);
    }

    @Transactional
    public void requiresManualReview(Long paymentId) {
        Payment payment = getById(paymentId);
        payment.marksAsRequiresReview();
        paymentRepository.save(payment);
    }

    private Payment getById(Long id) {
        return paymentRepository.findById(id).orElseThrow(() -> new IllegalStateException("결제 내역을 찾을 수 없습니다."));
    }
}
