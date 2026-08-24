package github.sangwook.ecommerce.order.port;

import github.sangwook.ecommerce.order.domain.AddressSnapshot;

public interface AddressPort {

    AddressSnapshot getAddressSnapshot(Long memberId, Long addressId);
}
