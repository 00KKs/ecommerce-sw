package github.sangwook.ecommerce.payment.application;

import github.sangwook.ecommerce.payment.PaymentStatus;
import github.sangwook.ecommerce.payment.domain.Payment;
import github.sangwook.ecommerce.payment.infrastructure.PaymentLookupResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentLookupScheduler {

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final PaymentService paymentService;

    private final PaymentConfirmResolver paymentConfirmResolver;

    @Scheduled(fixedDelay = 10_000)
    public void lookupUnknownPayments() {
        List<Payment> payments = paymentRepository.findAllByStatusAndNextCheckBefore(PaymentStatus.UNKNOWN, OffsetDateTime.now());
        payments.forEach(this::lookup);
    }

    //FIXME PaymentAdapter와 중복된 코드, jitter로 backoff 조정을 하지만 결국 요청을 한꺼번에 보내는 구조: RateLimiter 검토 가능
    private void lookup(Payment payment) {
        Long paymentId = payment.getId();
        String paymentKey = payment.getPaymentKey();
        switch (paymentGateway.lookupPayment(payment.getPaymentKey())) {
            case PaymentLookupResult.DONE(int doneAmount, OffsetDateTime approvedAt) ->  {
                Integer amount = payment.getAmount();
                if (doneAmount != amount) {
                    log.error("재확인 결과 금액 불일치. 기대={}, 실제={}, paymentKey={}", amount, doneAmount, paymentKey);
                    return;
                }
                log.info("재확인 결과 승인 완료 확인, 동기화 진행. paymentKey={}", paymentKey);
                paymentService.success(payment.getId());
            }
            case PaymentLookupResult.ABORTED() -> {
                log.info("재확인 결과 승인 실패 확인. paymentKey={}", paymentKey);
                paymentService.aborted(paymentId);
            }
            case PaymentLookupResult.CANCELED(int amount, OffsetDateTime approvedAt, OffsetDateTime canceledAt) -> {
                log.error("예상치 못한 상태(CANCELED) 확인. 수동 확인 필요. paymentKey={}, approvedAt={}, canceledAt={}", paymentKey, approvedAt, canceledAt);
                paymentService.requiresManualReview(paymentId);
            }
            case PaymentLookupResult.READY() -> {
                log.info("재확인 결과 아직 미승인. paymentKey={}", paymentKey);
                paymentConfirmResolver.resolve(paymentKey, payment.getOrderId(), payment.getAmount(), payment.getIdempotencyKey(), paymentId);
            }
            case PaymentLookupResult.NOT_FOUND() -> {
                log.info("재확인 결과 PG에 내역 없음. 수동 확인 필요. paymentKey={}", paymentKey);
                paymentService.requiresManualReview(paymentId);
            }
        }
    }
}
