package github.sangwook.ecommerce.member.application;

import github.sangwook.ecommerce.member.domain.Address;
import java.util.List;
import java.util.Optional;

public interface AddressRepository {

    Address save(Address address);

    List<Address> findAllByMemberId(Long memberId);

    Optional<Address> findById(Long id);

    void saveAll(List<Address> addresses);

    Optional<Address> findByIdAndMemberId(Long addressId, Long memberId);
}
