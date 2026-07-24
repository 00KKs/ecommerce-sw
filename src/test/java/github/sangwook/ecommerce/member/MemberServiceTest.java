package github.sangwook.ecommerce.member;

import github.sangwook.ecommerce.member.application.MemberRepository;
import github.sangwook.ecommerce.member.application.MemberService;
import github.sangwook.ecommerce.member.domain.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MemberServiceTest {

    private MemberRepository fakeMemberRepository;
    private MemberService memberService;

    @BeforeEach
    void before() {
        this.fakeMemberRepository = new FakeMemberRepository();
        this.memberService = new MemberService(fakeMemberRepository, new BCryptPasswordEncoder());
    }

    @Nested
    class 회원_가입 {

        @Test
        void 회원_가입_시_이메일이_이미_존재할_경우_예외를_던진다() {
            fakeMemberRepository.save(new Member("ssw@test.com", "passwordHash", "sangwook"));
            assertThatThrownBy(
                    () -> memberService.join("ssw@test.com", "pass", "sangwook")
            ).isInstanceOf(IllegalStateException.class);
        }
    }



}
