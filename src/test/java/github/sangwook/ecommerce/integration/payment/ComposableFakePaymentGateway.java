package github.sangwook.ecommerce.integration.payment;

import github.sangwook.ecommerce.payment.application.PaymentGateway;
import github.sangwook.ecommerce.payment.infrastructure.PaymentConfirmResult;
import github.sangwook.ecommerce.payment.infrastructure.PaymentInitiateResult;
import github.sangwook.ecommerce.payment.infrastructure.PaymentLookupResult;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ComposableFakePaymentGateway implements PaymentGateway {

    private final Map<String, FakePgPayment> storedPayments = new ConcurrentHashMap<>();

    private final InitiateScenario initiateScenario;
    private final ConfirmScenario confirmScenario;
    private final ConfirmScenario confirmRetryScenario;
    private final LookupScenario lookupScenario;

    public ComposableFakePaymentGateway(InitiateScenario initiateScenario, ConfirmScenario confirmScenario, ConfirmScenario confirmRetryScenario, LookupScenario lookupScenario) {
        this.initiateScenario = initiateScenario;
        this.confirmScenario = confirmScenario;
        this.confirmRetryScenario = confirmRetryScenario;
        this.lookupScenario = lookupScenario;
    }

    @Override
    public PaymentInitiateResult initiatePayment(Long orderId, Integer amount) {
        PaymentInitiateResult result = initiateScenario.apply(orderId, amount);
        if (result instanceof PaymentInitiateResult.SUCCESS success) {
            storedPayments.put(success.paymentKey(), new FakePgPayment(success.paymentKey(), orderId, amount));
        }
        return result;
    }

    @Override
    public PaymentConfirmResult confirmPayment(String paymentKey, Long orderId, int amount, UUID idempotencyKey) {
        int attempt = storedPayments.get(paymentKey).recordConfirm(idempotencyKey);
        ConfirmScenario scenario = attempt == 1 ? confirmScenario : confirmRetryScenario;
        return scenario.apply(paymentKey, orderId, amount);
    }

    @Override
    public PaymentLookupResult lookupPayment(String paymentKey) {
        FakePgPayment stored = storedPayments.get(paymentKey);
        if (stored == null) {
            return new PaymentLookupResult.NOT_FOUND();
        }
        return lookupScenario.respond(stored);
    }

    public FakePgPayment storedPayment(String paymentKey) {
        FakePgPayment stored = storedPayments.get(paymentKey);
        if (stored == null) {
            throw new IllegalStateException("initiate되지 않은 paymentKey: " + paymentKey);
        }
        return stored;
    }
}