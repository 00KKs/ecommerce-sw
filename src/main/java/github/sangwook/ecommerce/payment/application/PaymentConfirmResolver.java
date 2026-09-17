package github.sangwook.ecommerce.payment.application;

import github.sangwook.ecommerce.order.port.dto.PaymentResult;
import github.sangwook.ecommerce.payment.infrastructure.PaymentConfirmResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentConfirmResolver {

    private final PaymentGateway paymentGateway;
    private final PaymentService paymentService;

    public PaymentResult resolve(String paymentKey, Long orderId, int amount, UUID paymentIdempotencyKey, Long paymentId) {
        switch (paymentGateway.confirmPayment(paymentKey, orderId, amount, paymentIdempotencyKey)) {
            case PaymentConfirmResult.SUCCESS(Long paymentOrderId, int paymentAmount) -> {
                if (!paymentOrderId.equals(orderId) || paymentAmount != amount) {
                    log.error("PG 응답 값 불일치. orderId 기대={}, 실제={}, amount 기대={}, 실제={}", orderId, paymentOrderId, amount, paymentAmount);
                    paymentService.aborted(paymentId);
                    return new PaymentResult.PAYMENT_FAILED(new PaymentResult.PaymentFailedStage.PAYMENT_CONFIRM(paymentKey), false);
                }
                paymentService.success(paymentId);
                return new PaymentResult.SUCCESS(paymentKey);
            }
            case PaymentConfirmResult.FAILED(String reasonCode, String reasonMessage, boolean retryable) -> {
                paymentService.aborted(paymentId);
                return new PaymentResult.PAYMENT_FAILED(new PaymentResult.PaymentFailedStage.PAYMENT_CONFIRM(paymentKey), retryable);
            }
            case PaymentConfirmResult.UNKNOWN() -> {
                paymentService.unknown(paymentId);
                return new PaymentResult.PAYMENT_UNKNOWN(paymentKey);
            }
        }
    }
}
