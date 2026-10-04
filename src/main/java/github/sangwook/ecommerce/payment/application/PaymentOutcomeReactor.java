package github.sangwook.ecommerce.payment.application;

import github.sangwook.ecommerce.payment.port.OrderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentOutcomeReactor {

    private final OrderPort orderPort;

    public void react(Long orderId, PaymentResult paymentResult) {
        orderPort.applyPaymentOutcome(PaymentResultTranslator.translate(paymentResult), orderId);
    }

}
