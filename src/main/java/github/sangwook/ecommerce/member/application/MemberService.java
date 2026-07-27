package github.sangwook.ecommerce.member.application;

import github.sangwook.ecommerce.auth.MemberSession;
import github.sangwook.ecommerce.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public MemberSession login(String email, String password) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new IllegalStateException("이메일 또는 비밀번호가 잘못되었습니다."));
        if (!passwordEncoder.matches(password, member.getPasswordHash())) throw new IllegalStateException("이메일 또는 비밀번호가 잘못되었습니다.");
        return new MemberSession(member.getId(), member.getEmail(), member.getName());
    }
}
