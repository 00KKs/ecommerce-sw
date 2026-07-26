package github.sangwook.ecommerce.member.infrastructure;

import github.sangwook.ecommerce.member.domain.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressJpaRepository extends JpaRepository<Address, Long> {

}
