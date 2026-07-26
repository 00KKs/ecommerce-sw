package github.sangwook.ecommerce.member.application;

import github.sangwook.ecommerce.member.domain.Address;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    @Transactional
    public void create(Long memberId, String recipientName, String recipientPhone, String address, String deliveryRequest, boolean isDefault) {
        int count = addressRepository.countByMemberId(memberId);
        if (count >= 10) throw new IllegalStateException("배송지는 10개까지 생성할 수 있습니다.");
        Address newAddress = new Address(memberId, deliveryRequest, recipientPhone, recipientName, address, false);
        newAddress = addressRepository.save(newAddress);

        if (count == 0 || isDefault) {
            applyDefault(memberId, newAddress);
        }
    }

    private void applyDefault(Long memberId, Address target) {
        addressRepository.findByMemberIdAndIsDefaultTrue(memberId).ifPresent(
            address -> {
                address.unSetDefault();
                addressRepository.save(address);
            }
        );

        target.setAsDefault();
        addressRepository.save(target);
    }
}
