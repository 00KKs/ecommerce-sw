package github.sangwook.ecommerce.member.application;

import github.sangwook.ecommerce.member.domain.Member;

public interface MemberRepository {
    boolean existsByEmail(String email);

    Member save(Member member);
}
