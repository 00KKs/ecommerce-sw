package github.sangwook.ecommerce.integration.payment;

import github.sangwook.ecommerce.payment.application.PaymentGateway;
import github.sangwook.ecommerce.payment.infrastructure.PaymentConfirmResult;
import github.sangwook.ecommerce.payment.infrastructure.PaymentInitiateResult;
import java.util.UUID;

public class FakePaymentGatewayAlwaysFail implements PaymentGateway {

    @Override
    public PaymentInitiateResult initiatePayment(Long orderId, Integer amount) {
        return new PaymentInitiateResult.FAILED("failCode", "failMessage");
    }

    @Override
    public PaymentConfirmResult confirmPayment(String paymentKey, Long orderId, int amount, UUID idempotencyKey) {
        return new PaymentConfirmResult.FAILED("PAYMENT_REJECTED", "결제가 거절되었습니다.");
    }
}
