package github.sangwook.ecommerce.member.application;

import github.sangwook.ecommerce.member.api.dto.AddressResponse;
import github.sangwook.ecommerce.member.domain.Address;
import java.util.List;
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

    @Transactional
    public void update(Long memberId, Long addressId, String recipientName, String recipientPhone, String stringAddress, String deliveryRequest) {
        Address address = addressRepository.findById(addressId).orElseThrow(() -> new IllegalStateException("배송지를 찾을 수 없습니다."));
        if (!address.getMemberId().equals(memberId)) throw new IllegalStateException("권한이 없습니다.");
        address.update(recipientName, recipientPhone, stringAddress, deliveryRequest);
        addressRepository.save(address);
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> getList(Long memberId) {
        return addressRepository.findAllByMemberId(memberId)
            .stream()
            .map(a -> new AddressResponse(a.getId(), a.getRecipientName(), a.getRecipientPhone(), a.getAddress(), a.getDeliveryRequest(), a.getIsDefault()))
            .toList();
    }

    @Transactional
    public void delete(Long memberId, Long addressId) {
        Address address = addressRepository.findById(addressId).orElseThrow(() -> new IllegalStateException("배송지를 찾을 수 없습니다."));
        if (!address.getMemberId().equals(memberId)) throw new IllegalStateException("권한이 없습니다.");
        if (address.getIsDefault()) throw new IllegalStateException("기본 배송지는 삭제할 수 없습니다.");
        addressRepository.delete(address);
    }

    @Transactional
    public void updateDefault(Long memberId, Long addressId) {
        Address address = addressRepository.findById(addressId).orElseThrow(() -> new IllegalStateException("배송지를 찾을 수 없습니다."));
        if (!address.getMemberId().equals(memberId)) throw new IllegalStateException("권한이 없습니다.");
        applyDefault(memberId, address);
    }

    private void applyDefault(Long memberId, Address target) {
        if (target.getIsDefault()) return;

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
