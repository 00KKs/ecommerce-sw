package github.sangwook.ecommerce.order.port;

import github.sangwook.ecommerce.order.port.dto.AddressSnapshot;

public interface AddressPort {

    AddressSnapshot getAddressSnapshot(Long memberId, Long addressId);
}
