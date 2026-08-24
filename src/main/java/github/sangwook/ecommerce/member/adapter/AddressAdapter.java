package github.sangwook.ecommerce.member.adapter;

import github.sangwook.ecommerce.member.adapter.dto.AddressInfo;
import github.sangwook.ecommerce.member.application.AddressService;
import github.sangwook.ecommerce.order.domain.AddressSnapshot;
import github.sangwook.ecommerce.order.port.AddressPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class AddressAdapter implements AddressPort {

    private final AddressService addressService;

    @Override
    public AddressSnapshot getAddressSnapshot(Long memberId, Long addressId) {
        AddressInfo info = addressService.getAddress(memberId, addressId);
        return new AddressSnapshot(info.getRecipientName(), info.getRecipientPhone(), info.getAddress(), info.getDeliveryRequest());
    }
}
