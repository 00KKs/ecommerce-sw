package github.sangwook.ecommerce.integration.payment;

import github.sangwook.ecommerce.payment.application.PaymentGateway;
import github.sangwook.ecommerce.payment.infrastructure.PaymentConfirmResult;
import github.sangwook.ecommerce.payment.infrastructure.PaymentInitiateResult;
import java.util.UUID;

public class ComposableFakePaymentGateway implements PaymentGateway {

    private final InitiateScenario initiateScenario;
    private final ConfirmScenario confirmScenario;

    public ComposableFakePaymentGateway(InitiateScenario initiateScenario, ConfirmScenario confirmScenario) {
        this.initiateScenario = initiateScenario;
        this.confirmScenario = confirmScenario;
    }

    @Override
    public PaymentInitiateResult initiatePayment(Long orderId, Integer amount) {
        return initiateScenario.apply(orderId, amount);
    }

    @Override
    public PaymentConfirmResult confirmPayment(String paymentKey, Long orderId, int amount, UUID idempotencyKey) {
        return confirmScenario.apply(paymentKey, orderId, amount);
    }
}