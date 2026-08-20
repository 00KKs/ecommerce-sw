package github.sangwook.ecommerce.order.application;

import github.sangwook.ecommerce.order.api.dto.PlaceOrderResponse;
import github.sangwook.ecommerce.order.domain.AddressSnapshot;
import github.sangwook.ecommerce.order.domain.ProductSnapshots;
import github.sangwook.ecommerce.order.port.AddressPort;
import github.sangwook.ecommerce.order.port.ProductPort;
import github.sangwook.ecommerce.order.port.StockPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final AddressPort addressPort;
    private final StockPort stockPort;
    private final ProductPort productPort;

    public PlaceOrderResponse placeOrder(Long memberId, Long addressId, Map<Long, Integer> skuIdQuantityMap) {
        //배송지를 확인하고, 스냅샷을 가져온다
        AddressSnapshot addressSnapshot = addressPort.getAddressSnapshot(memberId, addressId);

        //상품 정보의 스냅샷을 가져온다
        ProductSnapshots productSnapshots = productPort.getProductSnapshots(skuIdQuantityMap);

        //재고를 줄인다

        //일단 재고까지 줄이는 것에 성공하면 주문의 결제 대기 상태까지는 완료되었고 커밋하면됨 - 트랜잭션 1 종료

        //PG사에 결제 요청을 한다 - 트랜잭션 외부

        //결제 완료

        //결제 정보 저장

        //응답 반환
    }
}
