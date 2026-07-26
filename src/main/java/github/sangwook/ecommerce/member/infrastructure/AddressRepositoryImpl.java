package github.sangwook.ecommerce.member.infrastructure;

import github.sangwook.ecommerce.member.application.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AddressRepositoryImpl implements AddressRepository {

    private final AddressJpaRepository addressJpaRepository;


}
