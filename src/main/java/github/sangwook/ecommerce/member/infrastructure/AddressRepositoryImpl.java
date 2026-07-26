package github.sangwook.ecommerce.member.infrastructure;

import github.sangwook.ecommerce.member.application.AddressRepository;
import github.sangwook.ecommerce.member.domain.Address;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AddressRepositoryImpl implements AddressRepository {

    private final AddressJpaRepository addressJpaRepository;

    @Override
    public int countByMemberId(Long memberId) {
        return addressJpaRepository.countByMemberId(memberId);
    }

    @Override
    public Address save(Address address) {
        return addressJpaRepository.save(address);
    }

    @Override
    public Optional<Address> findByMemberIdAndIsDefaultTrue(Long memberId) {
        return addressJpaRepository.findByMemberIdAndIsDefaultTrue(memberId);
    }

    @Override
    public List<Address> findAllByMemberId(Long memberId) {
        return addressJpaRepository.findAllByMemberId(memberId);
    }

    @Override
    public Optional<Address> findById(Long id) {
        return addressJpaRepository.findById(id);
    }
}
