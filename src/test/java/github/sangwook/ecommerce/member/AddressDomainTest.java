package github.sangwook.ecommerce.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import github.sangwook.ecommerce.member.domain.Address;
import github.sangwook.ecommerce.member.domain.AddressBook;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AddressDomainTest {

    private final AtomicLong idSeq = new AtomicLong(1L);

    private Address address(String name, boolean isDefault) {
        Address address = new Address(1L, "주소-" + name, "010-0000-0000", name, "요청", isDefault);
        ReflectionTestUtils.setField(address, "id", idSeq.getAndIncrement());
        return address;
    }

    private long countDefaults(AddressBook book) {
        return book.getAddresses().stream().filter(Address::getIsDefault).count();
    }

    @Nested
    class 추가 {

        @Test
        void 비어있을때_추가하면_isDefault가_false여도_기본이_된다() {
            AddressBook book = new AddressBook(new ArrayList<>());

            book.add(address("홍길동", false));
            assertThat(countDefaults(book)).isEqualTo(1);
        }

        @Test
        void 기본이_있을때_일반배송지를_추가하면_기존_기본이_유지된다() {
            AddressBook book = new AddressBook(new ArrayList<>(List.of(address("홍길동", true))));

            book.add(address("김철수", false));

            assertThat(countDefaults(book)).isEqualTo(1);
            Address def = book.getAddresses().stream().filter(Address::getIsDefault).findFirst().get();
            assertThat(def.getRecipientName()).isEqualTo("홍길동");
        }

        @Test
        void 기본으로_추가하면_기존_기본은_해제된다() {
            AddressBook book = new AddressBook(new ArrayList<>(List.of(address("홍길동", true))));

            book.add(address("김철수", true));

            assertThat(countDefaults(book)).isEqualTo(1);
            Address def = book.getAddresses().stream().filter(Address::getIsDefault).findFirst().get();
            assertThat(def.getRecipientName()).isEqualTo("김철수");
        }

        @Test
        void 최대_10개까지_추가할_수_있다() {
            AddressBook book = new AddressBook(new ArrayList<>());

            assertThatCode(() -> {
                for (int i = 0; i < 10; i++) {
                    book.add(address("수령인" + i, false));
                }
            }).doesNotThrowAnyException();

            assertThat(book.getAddresses()).hasSize(10);
        }

        @Test
        void _10개를_초과하여_추가하면_예외가_발생한다() {
            AddressBook book = new AddressBook(new ArrayList<>());
            for (int i = 0; i < 10; i++) {
                book.add(address("수령인" + i, false));
            }

            assertThatThrownBy(() -> book.add(address("초과", false)))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    class 삭제 {

        @Test
        void 존재하지_않는_배송지를_삭제하면_예외가_발생한다() {
            AddressBook book = new AddressBook(new ArrayList<>(
                    List.of(
                            address("홍길동", true),
                            address("김철수", false)
                    )
            ));

            assertThatThrownBy(() -> book.delete(9999L))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void 배송지가_한개일때_삭제하면_예외가_발생한다() {
            Address only = address("홍길동", true);
            AddressBook book = new AddressBook(new ArrayList<>(List.of(only)));

            assertThatThrownBy(() -> book.delete(only.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void 기본_배송지를_삭제하면_예외가_발생한다() {
            Address def = address("홍길동", true);
            AddressBook book = new AddressBook(new ArrayList<>(List.of(
                    def,
                    address("김철수", false)
            )));

            assertThatThrownBy(() -> book.delete(def.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void 기본이_아닌_배송지는_삭제할_수_있다() {
            Address def = address("홍길동", true);
            Address kim = address("김철수", false);
            AddressBook book = new AddressBook(new ArrayList<>(List.of(def, kim)));

            book.delete(kim.getId());

            assertThat(book.getAddresses()).hasSize(1);
            assertThat(countDefaults(book)).isEqualTo(1);
        }

        @Test
        void 다른_배송지를_기본으로_지정한_후_기존_기본을_삭제할_수_있다() {
            Address old = address("홍길동", true);
            Address kim = address("김철수", false);
            AddressBook book = new AddressBook(new ArrayList<>(List.of(old, kim)));

            book.changeDefault(kim.getId());
            book.delete(old.getId());

            assertThat(book.getAddresses()).hasSize(1);
            assertThat(countDefaults(book)).isEqualTo(1);
        }
    }

    @Nested
    class 기본_변경 {

        @Test
        void 기본을_변경하면_기존_기본은_해제된다() {
            Address hong = address("홍길동", true);
            Address kim = address("김철수", false);
            AddressBook book = new AddressBook(new ArrayList<>(List.of(hong, kim)));

            book.changeDefault(kim.getId());

            assertThat(hong.getIsDefault()).isFalse();
            assertThat(kim.getIsDefault()).isTrue();
            assertThat(countDefaults(book)).isEqualTo(1);
        }

        @Test
        void 이미_기본인_배송지를_다시_기본으로_지정해도_기본은_정확히_1개다() {
            Address hong = address("홍길동", true);
            Address kim = address("김철수", false);
            AddressBook book = new AddressBook(new ArrayList<>(List.of(hong, kim)));

            book.changeDefault(hong.getId());

            assertThat(hong.getIsDefault()).isTrue();
            assertThat(countDefaults(book)).isEqualTo(1);
        }

        @Test
        void 존재하지_않는_배송지를_기본으로_지정하면_예외가_발생한다() {
            AddressBook book = new AddressBook(new ArrayList<>(List.of(
                    address("홍길동", true)
            )));

            assertThatThrownBy(() -> book.changeDefault(9999L))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}