package github.sangwook.ecommerce.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import github.sangwook.ecommerce.member.application.AddressService;
import github.sangwook.ecommerce.member.domain.Address;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class AddressServiceTest {

    private AddressService addressService;
    private FakeAddressRepository fakeAddressRepository;

    @BeforeEach
    void setUp() {
        this.fakeAddressRepository = new FakeAddressRepository();
        this.addressService = new AddressService(fakeAddressRepository);
    }

    @Nested
    class 배송지_추가 {

        @Test
        void 배송지가_하나도_없는_상태에서_배송지_추가_시_기본_배송지로_지정된다() {
            addressService.create(1L, "홍길동", "010-1111-2222", "서울시", "문앞", false);

            Optional<Address> defaultAddress = fakeAddressRepository.findByMemberIdAndIsDefaultTrue(1L);
            assertThat(defaultAddress).isPresent();
            assertThat(defaultAddress.get().getRecipientName()).isEqualTo("홍길동");
            assertThat(fakeAddressRepository.findAllByMemberId(1L).stream().filter(Address::getIsDefault).count()).isEqualTo(1);
        }

        @Test
        void 기본_배송지가_있을때_일반_배송지를_추가하면_기존_기본은_유지된다() {
            addressService.create(1L, "홍길동", "010-1111-2222", "서울시", "문앞", false);
            addressService.create(1L, "김철수", "010-3333-4444", "부산시", "경비실", false);

            Optional<Address> defaultAddress = fakeAddressRepository.findByMemberIdAndIsDefaultTrue(1L);
            assertThat(defaultAddress).isPresent();
            assertThat(defaultAddress.get().getRecipientName()).isEqualTo("홍길동");
        }

        @Test
        void 배송지는_10개까지_추가_가능하다() {
            for (int i = 0; i < 10; i++) {
                addressService.create(1L, "수령인" + i, "010-0000-000" + i, "주소" + i, "요청", false);
            }

            assertThat(fakeAddressRepository.countByMemberId(1L)).isEqualTo(10);
        }

        @Test
        void 배송지_10개를_초과하여_추가하면_예외가_발생한다() {
            for (int i = 0; i < 10; i++) {
                addressService.create(1L, "수령인" + i, "010-0000-000" + i, "주소" + i, "요청", false);
            }

            assertThatThrownBy(() -> addressService.create(1L, "초과", "010-9999-9999", "주소", "요청", false))
                .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void 새_배송지를_기본으로_지정하면_이전_기본은_해제된다() {
            addressService.create(1L, "홍길동", "010-1111-2222", "서울시", "문앞", false);
            addressService.create(1L, "김철수", "010-3333-4444", "부산시", "경비실", true);

            Optional<Address> defaultAddress = fakeAddressRepository.findByMemberIdAndIsDefaultTrue(1L);
            assertThat(defaultAddress).isPresent();
            assertThat(defaultAddress.get().getRecipientName()).isEqualTo("김철수");
            List<Address> addresses = fakeAddressRepository.findAllByMemberId(1L).stream().filter(Address::getIsDefault).toList();
            assertThat(addresses.size()).isEqualTo(1);
            assertThat(addresses.get(0).getAddress()).isEqualTo("부산시");
        }

    }

    @Nested
    class 배송지_수정 {
        @Test
        void 배송지_수정_시_회원_아이디가_다를_경우_예외가_발생한다() {
            addressService.create(1L, "홍길동", "010-1111-2222", "서울시", "문앞", false);

            Address address = fakeAddressRepository.findAllByMemberId(1L).get(0);
            assertThatThrownBy(() -> addressService.update(2L, address.getId(), "김철수", "010-3333-4444", "부산시", "경비실"))
                .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    class 배송지_삭제 {

        @Test
        void 삭제할_배송지가_내_배송지가_아닌_경우_예외가_발생한다() {
            addressService.create(2L, "김철수", "010-3333-4444", "부산시", "경비실", false);
            Address only = fakeAddressRepository.findAllByMemberId(2L).get(0);

            assertThatThrownBy(() -> addressService.delete(1L, only.getId())).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void 배송지가_한개일때_삭제하면_예외가_발생한다() {
            addressService.create(1L, "홍길동", "010-1111-2222", "서울시", "문앞", false);
            Address only = fakeAddressRepository.findAllByMemberId(1L).get(0);

            assertThatThrownBy(() -> addressService.delete(1L, only.getId())).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void 기본_배송지를_삭제하면_예외가_발생한다() {
            addressService.create(1L, "홍길동", "010-1111-2222", "서울시", "문앞", false);
            addressService.create(1L, "김철수", "010-3333-4444", "부산시", "경비실", false);
            Address defaultAddr = fakeAddressRepository.findByMemberIdAndIsDefaultTrue(1L).get();

            assertThatThrownBy(() -> addressService.delete(1L, defaultAddr.getId())).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void 다른_배송지를_기본으로_지정한_후_기존_기본을_삭제하면_성공한다() {
            addressService.create(1L, "홍길동", "010-1111-2222", "서울시", "문앞", false);
            addressService.create(1L, "김철수", "010-3333-4444", "부산시", "경비실", false);
            Address old = fakeAddressRepository.findByMemberIdAndIsDefaultTrue(1L).get();
            Address address = fakeAddressRepository.findAllByMemberId(1L).stream().filter(a -> a.getIsDefault() == false).findFirst().get();

            addressService.updateDefault(1L, address.getId());

            assertDoesNotThrow(() -> addressService.delete(1L, old.getId()));
        }

        @Test
        void 기본이_아닌_배송지는_삭제_가능하다() {
            addressService.create(1L, "홍길동", "010-1111-2222", "서울시", "문앞", false);
            addressService.create(1L, "김철수", "010-3333-4444", "부산시", "경비실", false);
            Address kim = fakeAddressRepository.findAllByMemberId(1L).stream().filter(a -> a.getRecipientName().equals("김철수")).findFirst().get();

            addressService.delete(1L, kim.getId());

            assertThat(fakeAddressRepository.countByMemberId(1L)).isEqualTo(1);
            assertThat(fakeAddressRepository.findByMemberIdAndIsDefaultTrue(1L)).isPresent();
        }

    }

    @Nested
    class 기본_배송지_지정 {

        @Test
        void 기본_배송지를_변경할_배송지가_내_배송지가_아닌_경우_예외가_발생한다() {
            addressService.create(1L, "홍길동", "010-1111-2222", "서울시", "문앞", false);
            Address addr = fakeAddressRepository.findAllByMemberId(1L).get(0);

            assertThatThrownBy(() -> addressService.updateDefault(2L, addr.getId())).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void 새_배송지를_기본으로_지정하면_이전_기본은_해제된다() {
            addressService.create(1L, "홍길동", "010-1111-2222", "서울시", "문앞", false);
            addressService.create(1L, "김철수", "010-3333-4444", "부산시", "경비실", false);
            Address kim = fakeAddressRepository.findAllByMemberId(1L).stream().filter(a -> a.getRecipientName().equals("김철수")).findFirst().get();

            addressService.updateDefault(1L, kim.getId());

            assertThat(fakeAddressRepository.findByMemberIdAndIsDefaultTrue(1L).get().getRecipientName()).isEqualTo("김철수");
            assertThat(countDefaults(1L)).isEqualTo(1);
        }

        @Test
        void 기본_배송지를_다시_기본으로_지정해도_기본으로_유지된다() {
            addressService.create(1L, "홍길동", "010-1111-2222", "서울시", "문앞", false);
            Address hong = fakeAddressRepository.findByMemberIdAndIsDefaultTrue(1L).get();

            addressService.updateDefault(1L, hong.getId());

            assertThat(fakeAddressRepository.findByMemberIdAndIsDefaultTrue(1L).get().getRecipientName()).isEqualTo("홍길동");
            assertThat(countDefaults(1L)).isEqualTo(1);
        }

        private long countDefaults(Long memberId) {
            return fakeAddressRepository.findAllByMemberId(memberId).stream()
                .filter(Address::getIsDefault)
                .count();
        }
    }

}
