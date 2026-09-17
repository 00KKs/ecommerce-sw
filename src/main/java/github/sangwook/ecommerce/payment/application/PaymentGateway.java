package github.sangwook.ecommerce.payment.application;

import github.sangwook.ecommerce.payment.infrastructure.PaymentConfirmResult;
import github.sangwook.ecommerce.payment.infrastructure.PaymentInitiateResult;

import github.sangwook.ecommerce.payment.infrastructure.PaymentLookupResult;
import java.util.UUID;

public interface PaymentGateway {

    PaymentInitiateResult initiatePayment(Long orderId, Integer amount);

    PaymentConfirmResult confirmPayment(String paymentKey, Long orderId, int amount, UUID idempotencyKey);

    PaymentLookupResult lookupPayment(String paymentKey);

}
