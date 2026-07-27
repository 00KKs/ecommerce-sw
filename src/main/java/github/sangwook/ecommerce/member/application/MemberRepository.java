package github.sangwook.ecommerce.member.application;

import github.sangwook.ecommerce.member.domain.Member;
import java.util.Optional;

public interface MemberRepository {
    boolean existsByEmail(String email);

    Member save(Member member);

    Optional<Member> findByEmail(String email);
}
