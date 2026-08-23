package github.sangwook.ecommerce.order.api;

import github.sangwook.ecommerce.auth.LoginMember;
import github.sangwook.ecommerce.auth.MemberSession;
import github.sangwook.ecommerce.order.api.dto.PlaceOrderRequest;
import github.sangwook.ecommerce.order.api.dto.PlaceOrderResponse;
import github.sangwook.ecommerce.order.application.PlaceOrderUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final PlaceOrderUseCase placeOrderUseCase;

    @PostMapping("/api/v1/orders")
    public ResponseEntity<PlaceOrderResponse> placeOrder(@LoginMember MemberSession memberSession, @RequestBody PlaceOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                placeOrderUseCase.placeOrder(
                        memberSession.getId(),
                        request.getAddressId(),
                        Map.of(request.getSkuId(), request.getQuantity())
                ));
    }
}
