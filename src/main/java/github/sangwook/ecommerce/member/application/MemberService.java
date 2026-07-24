package github.sangwook.ecommerce.member.application;

import github.sangwook.ecommerce.member.domain.Member;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void join(String email, String password, String name) {
        if (memberRepository.existsByEmail(email)) throw new IllegalStateException("사용 중인 이메일입니다.");
        Member member = new Member(email, passwordEncoder.encode(password), name);
        memberRepository.save(member);
    }
}
