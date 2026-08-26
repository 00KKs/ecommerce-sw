package github.sangwook.ecommerce.payment;

import github.sangwook.ecommerce.order.port.PaymentPort;
import github.sangwook.ecommerce.order.port.dto.PaymentResult;
import github.sangwook.ecommerce.payment.infrastructure.PaymentGateway;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class PaymentAdapter implements PaymentPort {

    private final PaymentService paymentService;
    private final PaymentGateway paymentGateway;

    //결제에 대한 pg사 api 요청과, 멱등키 생성, 그 결과의 저장이 이루어진다
    @Override
    public PaymentResult processPayment(Long orderId, int amount) {
        String paymentKey = paymentGateway.initiatePayment(orderId, amount);

        //pg 결제 요청
        //멱등키 생성
        //pg 결제 승인
        //응답 반환
        return new PaymentResult(UUID.randomUUID().toString());
    }
}
