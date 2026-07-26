package github.sangwook.ecommerce.member.application;

import github.sangwook.ecommerce.member.domain.Address;
import java.util.List;
import java.util.Optional;

public interface AddressRepository {

    int countByMemberId(Long memberId);

    Address save(Address address);

    Optional<Address> findByMemberIdAndIsDefaultTrue(Long memberId);

    List<Address> findAllByMemberId(Long memberId);
}
