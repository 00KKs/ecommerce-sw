package github.sangwook.ecommerce.payment.application;

import github.sangwook.ecommerce.payment.domain.Payment;
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

    public PaymentResult processPayment(Long paymentId) {
        Payment payment = paymentService.getById(paymentId);
        PaymentInitiateResult.SUCCESS initiated;
        switch (paymentGateway.initiatePayment(payment.getOrderId(), payment.getAmount())) {
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
                paymentId, payment.getOrderId(), e.getCurrentStatus(), initiated.paymentKey());
            return new PaymentResult.PAYMENT_INITIATE_FAILED();
        } catch (RuntimeException e) {
            log.error("paymentKey 할당 실패", e);
            paymentService.aborted(paymentId);
            return new PaymentResult.PAYMENT_INITIATE_FAILED();
        }

        return paymentConfirmResolver.resolve(paymentKey, payment.getOrderId(), payment.getAmount(), payment.getIdempotencyKey(), paymentId);
    }

}
