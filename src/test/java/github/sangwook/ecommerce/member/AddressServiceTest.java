package github.sangwook.ecommerce.member;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class AddressServiceTest {

    @Nested
    class 배송지_추가 {

        @Test
        void 배송지가_하나도_없는_상태에서_배송지_추가_시_기본_배송지로_지정된다() {

        }

        @Test
        void 기본_배송지가_있을때_일반_배송지를_추가하면_기존_기본은_유지된다() {

        }

        @Test
        void 배송지는_10개까지_추가_가능하다() {

        }

        @Test
        void 배송지_10개를_초과하여_추가하면_예외가_발생한다() {

        }

        @Test
        void 새_배송지를_기본으로_지정하면_이전_기본은_해제된다() {

        }

    }

    @Nested
    class 배송지_삭제 {

        @Test
        void 배송지가_한개일때_삭제하면_예외가_발생한다() {

        }

        @Test
        void 배송지가_여러개일때_기본_배송지를_삭제하면_예외가_발생한다() {

        }

        @Test
        void 다른_배송지를_기본으로_지정한_후_기존_기본을_삭제하면_성공한다() {

        }

        @Test
        void 기본이_아닌_배송지는_삭제_가능하다() {

        }

    }

    @Nested
    class 기본_배송지_지정 {

        @Test
        void 새_배송지를_기본으로_지정하면_이전_기본은_해제된다() {

        }
    }

}
