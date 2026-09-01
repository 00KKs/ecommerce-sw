package github.sangwook.ecommerce.payment;

import github.sangwook.ecommerce.order.port.PaymentPort;
import github.sangwook.ecommerce.order.port.dto.PaymentResult;
import java.util.UUID;

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
        //결제 요청

        //결제 요청 성공 -> 결제 요청 정보 저장
        //결제 요청 실패
        //  -> pg사 문제 or 네트워크 문제
        //      -> 재시도
        //  -> 요청 정보 문제
        //      -> 결제 취소
        String paymentKey;
        switch (paymentGateway.initiatePayment(orderId, amount)) {
            case PaymentInitiateResult.SUCCESS(String key) -> paymentKey = key;
            case PaymentInitiateResult.FAILED(String reasonCode, String reasonMessage) -> {
                return new PaymentResult.FAILED();
            }
            case PaymentInitiateResult.UNKNOWN(Throwable cause) -> {
                return new PaymentResult.FAILED();
            }
        }

        //결제 대기 상태 저장

        UUID paymentIdempotencyKey = UUID.randomUUID();
        switch (paymentGateway.confirmPayment(paymentKey, orderId, amount, paymentIdempotencyKey)) {
            case PaymentConfirmResult.SUCCESS() -> {
                //결제 승인 결과 갱신
                return new PaymentResult.SUCCESS(paymentKey);
            }
            case PaymentConfirmResult.FAILED(String reasonCode, String reasonMessage) -> {
                return new PaymentResult.FAILED();
            }
            case PaymentConfirmResult.UNKNOWN(Throwable cause) -> {
                return new PaymentResult.FAILED();
            }
        }
    }
}
