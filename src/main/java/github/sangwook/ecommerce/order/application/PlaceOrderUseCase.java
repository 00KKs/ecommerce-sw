package github.sangwook.ecommerce.order.application;

import github.sangwook.ecommerce.order.api.dto.PlaceOrderResponse;
import github.sangwook.ecommerce.order.api.mapper.PlaceOrderResponseMapper;
import github.sangwook.ecommerce.order.port.dto.AddressSnapshot;
import github.sangwook.ecommerce.order.domain.Order;
import github.sangwook.ecommerce.order.port.dto.ProductSnapshots;
import github.sangwook.ecommerce.order.port.AddressPort;
import github.sangwook.ecommerce.order.port.PaymentPort;
import github.sangwook.ecommerce.order.port.ProductPort;
import github.sangwook.ecommerce.order.port.dto.PaymentResult;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceOrderUseCase {

    private final OrderService orderService;

    private final PaymentPort paymentPort;
    private final AddressPort addressPort;
    private final ProductPort productPort;

    private final TransactionTemplate transactionTemplate;

    public PlaceOrderResponse placeOrder(Long memberId, Long addressId, Map<Long, Integer> skuIdQuantityMap) {
        AddressSnapshot addressSnapshot = addressPort.getAddressSnapshot(memberId, addressId);
        ProductSnapshots productSnapshots = productPort.getProductSnapshots(skuIdQuantityMap);

        PreparedOrder prepared = transactionTemplate.execute(status -> {
            Order order = orderService.createOrder(memberId, addressSnapshot, productSnapshots, skuIdQuantityMap);
            Long paymentId = paymentPort.prepare(order.getId(), order.getTotalPrice());
            return new PreparedOrder(order.getId(), paymentId);
        });

        PaymentResult paymentResult = paymentPort.processPayment(prepared.paymentId());

        Order order = orderService.applyPaymentOutcome(paymentResult, prepared.orderId());
        return PlaceOrderResponseMapper.buildResponse(order, paymentResult.paymentKey(), addressSnapshot);
    }

    private record PreparedOrder(Long orderId, Long paymentId) {}
}
