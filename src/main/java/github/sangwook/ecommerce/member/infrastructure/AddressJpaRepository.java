package github.sangwook.ecommerce.member.infrastructure;

import github.sangwook.ecommerce.member.domain.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressJpaRepository extends JpaRepository<Address, Long> {

    List<Address> findAllByMemberId(Long memberId);
}
