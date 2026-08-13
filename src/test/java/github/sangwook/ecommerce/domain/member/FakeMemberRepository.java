package github.sangwook.ecommerce.domain.member;

import github.sangwook.ecommerce.member.application.MemberRepository;
import github.sangwook.ecommerce.member.domain.Member;
import java.util.Optional;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class FakeMemberRepository implements MemberRepository {

    private final AtomicLong ID_COUNTER = new AtomicLong(1L);
    private final Map<Long, Member> map = new ConcurrentHashMap<>();

    @Override
    public boolean existsByEmail(String email) {
        for (Member value : map.values()) {
            if (value.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Member save(Member member) {
        if (member.getId() == null) {
            injectId(member, ID_COUNTER.getAndIncrement());
        }
        map.put(member.getId(), member);
        return member;
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        for (Member value : map.values()) {
            if (value.getEmail().equals(email)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    private void injectId(Member member, Long id) {
        ReflectionTestUtils.setField(member, "id", id);
    }
}
