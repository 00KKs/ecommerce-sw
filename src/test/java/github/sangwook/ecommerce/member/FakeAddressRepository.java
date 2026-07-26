package github.sangwook.ecommerce.member;

import github.sangwook.ecommerce.member.application.AddressRepository;
import github.sangwook.ecommerce.member.domain.Address;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.test.util.ReflectionTestUtils;

public class FakeAddressRepository implements AddressRepository {

    private final AtomicLong ID_COUNTER = new AtomicLong(1L);
    private final Map<Long, Address> map = new ConcurrentHashMap<>();

    @Override
    public int countByMemberId(Long memberId) {
        int counter = 0;
        for (Address value : map.values()) {
            if (value.getMemberId().equals(memberId)) counter++;
        }
        return counter;
    }

    @Override
    public Address save(Address address) {
        if (address.getId() == null) {
            injectId(address, ID_COUNTER.getAndIncrement());
        }
        map.put(address.getId(), address);
        return address;
    }

    @Override
    public Optional<Address> findByMemberIdAndIsDefaultTrue(Long memberId) {
        for (Address value : map.values()) {
            if (memberId.equals(value.getMemberId()) && value.getIsDefault()) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Address> findAllByMemberId(Long memberId) {
        List<Address> addresses = new ArrayList<>();
        for (Address value : map.values()) {
            if (value.getMemberId().equals(memberId)) addresses.add(value);
        }
        return addresses;
    }

    private void injectId(Address address, Long id) {
        ReflectionTestUtils.setField(address, "id", id);
    }
}
