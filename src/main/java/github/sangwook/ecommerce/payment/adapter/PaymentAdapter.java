package github.sangwook.ecommerce.payment.adapter;

import github.sangwook.ecommerce.order.port.PaymentPort;
import github.sangwook.ecommerce.order.port.dto.PaymentResult;
import github.sangwook.ecommerce.payment.application.PaymentGatewayHandler;
import github.sangwook.ecommerce.payment.application.PaymentResultTranslator;
import github.sangwook.ecommerce.payment.application.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
class PaymentAdapter implements PaymentPort {

    private final PaymentService paymentService;
    private final PaymentGatewayHandler paymentGatewayHandler;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Long prepare(Long orderId, int amount) {
        UUID paymentIdempotencyKey = UUID.randomUUID();
        return paymentService.ready(orderId, amount, paymentIdempotencyKey);
    }

    //이 요청 자체도 중복되어 들어올 수 있으므로 주문하기에서도 멱등성 보장이 필요하다
    @Override
    public PaymentResult processPayment(Long paymentId) {
        return PaymentResultTranslator.translate(paymentGatewayHandler.processPayment(paymentId));
    }
}
