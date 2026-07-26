package github.sangwook.ecommerce.member.infrastructure;

import github.sangwook.ecommerce.member.domain.Address;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressJpaRepository extends JpaRepository<Address, Long> {

    int countByMemberId(Long memberId);

    Optional<Address> findByMemberIdAndIsDefaultTrue(Long memberId);

    List<Address> findAllByMemberId(Long memberId);
}
