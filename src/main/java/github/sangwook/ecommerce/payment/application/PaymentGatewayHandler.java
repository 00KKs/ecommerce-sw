package github.sangwook.ecommerce.payment.application;

import github.sangwook.ecommerce.payment.exception.InvalidPaymentStateException;
import github.sangwook.ecommerce.payment.exception.PaymentMismatchException;
import github.sangwook.ecommerce.payment.infrastructure.PaymentInitiateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentGatewayHandler {

    private final PaymentService paymentService;
    private final PaymentGateway paymentGateway;
    private final PaymentConfirmResolver paymentConfirmResolver;

    public PaymentResult processPayment(Long orderId, int amount) {
        UUID paymentIdempotencyKey = UUID.randomUUID();
        Long paymentId = paymentService.ready(orderId, amount, paymentIdempotencyKey);

        PaymentInitiateResult.SUCCESS initiated;
        switch (paymentGateway.initiatePayment(orderId, amount)) {
            case PaymentInitiateResult.SUCCESS success -> initiated = success;
            case PaymentInitiateResult.FAILED(String reasonCode, String reasonMessage) -> {
                //initiate 실패는 재시도 없음 - 실제 흐름 고려한 선택
                return new PaymentResult.PAYMENT_INITIATE_FAILED();
            }
            case PaymentInitiateResult.UNKNOWN() -> {
                return new PaymentResult.PAYMENT_INITIATE_FAILED();
            }
        }

        String paymentKey = initiated.paymentKey();
        try {
            paymentService.updatePaymentKey(
                paymentId,
                Long.valueOf(initiated.orderId()),
                initiated.amount(),
                paymentKey
            );
        } catch (PaymentMismatchException e) {
            log.error("결제 정보 불일치: {}, expected={}, actual={}", e.getField(), e.getExpected(), e.getActual());
            return new PaymentResult.PAYMENT_INITIATE_FAILED();
        } catch (InvalidPaymentStateException e) {
            log.warn("paymentKey 할당 실패: Payment가 할당 가능한 상태가 아님. paymentId={}, orderId={}, currentStatus={}, paymentKey={}",
                paymentId, orderId, e.getCurrentStatus(), initiated.paymentKey());
            return new PaymentResult.PAYMENT_INITIATE_FAILED();
        }

        return paymentConfirmResolver.resolve(paymentKey, orderId, amount, paymentIdempotencyKey, paymentId);
    }

}
