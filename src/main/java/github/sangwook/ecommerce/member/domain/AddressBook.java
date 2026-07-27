package github.sangwook.ecommerce.member.domain;

import lombok.Getter;

import java.util.List;

public class AddressBook {

    private static final int MAX_SIZE = 10;

    @Getter
    private final List<Address> addresses;

    public AddressBook(List<Address> addresses) {
        this.addresses = addresses;
    }

    public void add(Address address) {
        int size = getSize();
        if (size >= MAX_SIZE) throw new IllegalStateException("배송지는 " + MAX_SIZE + "개까지 생성할 수 있습니다.");

        if (addresses.isEmpty()) {
            address.setAsDefault();
            addresses.add(address);
            return;
        }

        if (address.getIsDefault()) {
            addresses.forEach(Address::unSetDefault);
            address.setAsDefault();
        }
        addresses.add(address);
    }

    public void delete(Long addressId) {
        Address target = findById(addressId);

        if (getSize() == 1) throw new IllegalStateException("배송지가 1개일 때는 삭제할 수 없습니다.");
        if (target.getIsDefault()) throw new IllegalStateException("기본 배송지는 삭제할 수 없습니다.");
        addresses.remove(target);
    }

    public void changeDefault(Long addressId) {
        Address target = findById(addressId);

        if (target.getIsDefault()) return;
        addresses.stream().filter(Address::getIsDefault).findFirst().ifPresent(Address::unSetDefault);
        target.setAsDefault();
    }

    private Address findById(Long id) {
        return addresses.stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("배송지를 찾을 수 없습니다."));
    }

    private int getSize() {
        return addresses.size();
    }
}
