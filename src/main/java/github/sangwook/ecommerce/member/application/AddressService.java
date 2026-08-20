package github.sangwook.ecommerce.member.application;

import github.sangwook.ecommerce.member.api.dto.AddressResponse;
import github.sangwook.ecommerce.member.domain.Address;
import java.util.List;

import github.sangwook.ecommerce.member.domain.AddressBook;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    @Transactional
    public void create(Long memberId, String recipientName, String recipientPhone, String address, String deliveryRequest, boolean isDefault) {
        AddressBook book = new AddressBook(addressRepository.findAllByMemberId(memberId));
        book.add(new Address(memberId, deliveryRequest, recipientPhone, recipientName, address, isDefault));
        addressRepository.saveAll(book.getAddresses());
    }

    @Transactional
    public void update(Long memberId, Long addressId, String recipientName, String recipientPhone, String stringAddress, String deliveryRequest) {
        Address address = getByIdAndMemberId(addressId, memberId);
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
        AddressBook book = new AddressBook(addressRepository.findAllByMemberId(memberId));
        book.delete(addressId);
        addressRepository.saveAll(book.getAddresses());
    }

    @Transactional
    public void changeDefault(Long memberId, Long addressId) {
        AddressBook book = new AddressBook(addressRepository.findAllByMemberId(memberId));
        book.changeDefault(addressId);
        addressRepository.saveAll(book.getAddresses());
    }
    private Address getByIdAndMemberId(Long addressId, Long memberId) {
        return addressRepository.findByIdAndMemberId(addressId, memberId).orElseThrow(() -> new IllegalStateException("배송지를 찾을 수 없습니다."));
    }
}
