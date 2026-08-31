package github.sangwook.ecommerce.payment;

import github.sangwook.ecommerce.order.port.PaymentPort;
import github.sangwook.ecommerce.order.port.dto.PaymentResult;
import java.util.UUID;

import github.sangwook.ecommerce.payment.infrastructure.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class PaymentAdapter implements PaymentPort {

    private final PaymentService paymentService;
    private final PaymentGateway paymentGateway;

    //이 요청 자체도 중복되어 들어올 수 있으므로 주문하기에서도 멱등성 보장이 필요하다
    @Override
    public PaymentResult processPayment(Long orderId, int amount) {
        String paymentKey = paymentGateway.initiatePayment(orderId, amount);
        UUID paymentIdempotencyKey = UUID.randomUUID();
        PaymentStatus paymentStatus = paymentGateway.confirmPayment(paymentKey, orderId, amount, paymentIdempotencyKey);

        //타임아웃 뜨면? 바로 재조회하여 결과 확인하기, 결과는 반드시 SUCCESS 또는 FAILED로 반환
        PaymentResult.Result result = paymentStatus == PaymentStatus.DONE ? PaymentResult.Result.SUCCESS : PaymentResult.Result.FAILED; //FIXME
        return new PaymentResult(paymentKey, result);
    }
}
