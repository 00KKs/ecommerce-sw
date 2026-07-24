package github.sangwook.ecommerce.member.infrastructure;

import github.sangwook.ecommerce.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);
}
