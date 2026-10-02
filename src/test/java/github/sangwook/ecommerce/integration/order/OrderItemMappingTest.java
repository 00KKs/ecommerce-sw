package github.sangwook.ecommerce.integration.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import github.sangwook.ecommerce.integration.AbstractIntegrationTest;
import github.sangwook.ecommerce.order.domain.Order;
import github.sangwook.ecommerce.order.domain.OrderStatus;
import github.sangwook.ecommerce.order.port.dto.ShippingAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

/**
 * Order -> OrderItem 단방향 @OneToMany(@JoinColumn) 매핑이
 * 실제로 어떤 SQL을 내보내는지 관찰하는 학습 테스트.
 *
 * 사용법:
 *   1) 현재 매핑 그대로 실행 -> 콘솔에 찍힌 SQL 확인
 *   2) @JoinColumn(name = "order_id", nullable = false, updatable = false) 로 바꾸고 다시 실행
 *   3) 두 결과의 insert/update/delete 개수 비교
 */
@DataJpaTest(
        showSql = false,
        properties = {
                "spring.jpa.properties.hibernate.session_factory.statement_inspector=github.sangwook.ecommerce.integration.order.SqlCaptor",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.properties.hibernate.format_sql=true",
                "logging.level.org.hibernate.SQL=debug",
                "logging.level.org.hibernate.orm.jdbc.bind=trace"
        })
class OrderItemMappingTest extends AbstractIntegrationTest {

    @Autowired
    TestEntityManager em;

    @BeforeEach
    void setUp() {
        SqlCaptor.clear();
    }

    @Test
    @DisplayName("주문 저장 시 order_item INSERT/UPDATE가 어떻게 나가는지 확인")
    void persist() {
        Order order = createOrderWithItems(3);

        em.persist(order);
        em.flush();   // 컬렉션 처리(FK 세팅)는 flush 시점에 일어난다

        SqlCaptor.print("persist + flush");

        // 어떤 방식이든 결과적으로 FK는 제대로 들어가 있어야 한다
        em.clear();
        List<?> orderIds = em.getEntityManager()
            .createNativeQuery("select order_id from order_item")
            .getResultList();
        assertThat(orderIds).hasSize(3)
            .allSatisfy(id -> assertThat(((Number) id).longValue()).isEqualTo(order.getId()));
    }

    @Test
    @DisplayName("컬렉션에서 항목 제거 시 (orphanRemoval) SQL 확인")
    void removeItem() {
        Order order = createOrderWithItems(3);
        em.persist(order);
        em.flush();
        em.clear();

        Order found = em.find(Order.class, order.getId());
        SqlCaptor.clear();

        found.getOrderItems().remove(0);
        em.flush();

        SqlCaptor.print("remove one item + flush");
        // nullable FK면 UPDATE ... set order_id=null 후 DELETE, non-null FK면 DELETE만 나가는지 비교해 볼 것 -> 확인완료 O
        assertThat(SqlCaptor.count("delete from order_item")).isEqualTo(1);
    }

    private Order createOrderWithItems(int count) {
        Order order = new Order(
            OrderStatus.PAYMENT_PENDING,
            10_000 * count,
            new ShippingAddress("홍길동", "010-1234-5678", "서울시 어딘가 123", "문 앞에 놔주세요")
        );
        for (int i = 1; i <= count; i++) {
            order.addOrderItem((long) i, "상품" + i, "옵션" + i, 10_000, 1);
        }
        return order;
    }
}