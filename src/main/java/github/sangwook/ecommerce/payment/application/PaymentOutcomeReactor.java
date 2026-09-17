package github.sangwook.ecommerce.payment.application;

import github.sangwook.ecommerce.order.port.dto.PaymentResult;
import github.sangwook.ecommerce.payment.port.OrderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentOutcomeReactor {

    private final OrderPort orderPort;

    public void react(Long orderId, PaymentResult paymentResult) {
        switch (paymentResult) {
            case PaymentResult.SUCCESS(String paymentKey) -> {
                orderPort.confirmOrder(orderId);
            }
            case PaymentResult.PAYMENT_FAILED(PaymentResult.PaymentFailedStage stage, boolean retryable) -> {
                if (!retryable) {
                    orderPort.failOrder(orderId);
                }
            }
            case PaymentResult.PAYMENT_UNKNOWN(String paymentKey) -> {
            }
        }
    }

}
