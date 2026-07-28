package github.sangwook.ecommerce.member.infrastructure;

import github.sangwook.ecommerce.member.application.AddressRepository;
import github.sangwook.ecommerce.member.domain.Address;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AddressRepositoryImpl implements AddressRepository {

    private final AddressJpaRepository addressJpaRepository;

    @Override
    public Address save(Address address) {
        return addressJpaRepository.save(address);
    }

    @Override
    public List<Address> findAllByMemberId(Long memberId) {
        return addressJpaRepository.findAllByMemberId(memberId);
    }

    @Override
    public Optional<Address> findById(Long id) {
        return addressJpaRepository.findById(id);
    }

    @Override
    public void saveAll(List<Address> addresses) {
        addressJpaRepository.saveAll(addresses);
    }
}
