# ADR-0010: Order–OrderItem 단방향 @OneToMany에 `nullable = false, updatable = false` 적용

## 상태
채택

## 배경 (이걸 왜 고민했고, 어떤 상황이었는가)
주문(Order) 도메인을 설계하면서 `Order`만 `orderItems`를 들고, `OrderItem`은 `Order`를 전혀 모르는 단방향 `@OneToMany` 구조로 매핑했다.  
OrderItem은 반드시 Order를 통해서만 생성·변경되어야 하므로, OrderItem 쪽에 Order 참조를 두지 않는 것이 경계를 지킬 수 있다고 판단했다.

그런데 이 매핑이 실제로 어떤 SQL을 내보내는지 확인하려고 `@DataJpaTest` + Testcontainers 환경에서 `StatementInspector`로 SQL을 가로채는 학습 테스트를 작성해 돌려봤다.

기본 매핑(`@JoinColumn(name = "order_id")`)으로 주문 항목 3개를 저장한 결과는 다음과 같았다.

    insert into orders (...)                         -- 1건
    insert into order_item (option_name, ...)        -- 3건, order_id 없음
    update order_item set order_id=? where id=?      -- 3건

- OrderItem 엔티티에는 `order_id` 필드가 없으므로, 엔티티 INSERT 시점에는 FK를 비운 채 저장된다.
- FK는 Order의 컬렉션이 소유하므로, flush 시 컬렉션 처리 단계에서 **항목마다 UPDATE로 뒤늦게 채워진다.**
- 생성된 DDL도 `order_id bigint`로 NULL을 허용했다. 즉 DB 레벨에서는 "주문 없는 주문 항목"이 들어갈 수 있는 상태였다.

결국 주문 항목이 N개면 INSERT N + UPDATE N이 나가고, 도메인상 절대 있으면 안 되는 상태(order_id = NULL)를 DB가 허용하고 있었다.

## 결정
단방향 구조는 유지하고, 조인 컬럼을 다음과 같이 설정한다.

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    private List<OrderItem> orderItems = new ArrayList<>();

## 이유
- 같은 테스트로 옵션별 차이를 직접 확인했다.
    - `nullable = false`: DDL이 `order_id bigint not null`로 바뀌고, OrderItem INSERT에 `order_id`가 바로 포함된다.
    - `updatable = false`: FK를 다시 세팅하는 UPDATE가 사라진다. 결과적으로 **INSERT N건만** 나간다.
    - 항목 제거 시에도 nullable FK일 때는 `UPDATE ... SET order_id = NULL` 후 DELETE가 나갔지만, non-null FK에서는 `orphanRemoval`에 의한 DELETE만 나갔다.
- "OrderItem은 반드시 하나의 Order에 속하고, 다른 주문으로 옮겨가지 않는다"는 도메인 규칙이 매핑과 DB 제약(NOT NULL)에 그대로 표현된다.
- OrderItem이 Order를 모르는 구조를 유지할 수 있어, Order을 통해서만 OrderItem 항목을 다루는 설계 의도를 해치지 않는다.

## 검토한 대안들
- **기본 매핑 유지 (`@JoinColumn(name = "order_id")`)** — 직접 확인한 문제. 항목마다 불필요한 UPDATE가 추가되고, FK가 NULL을 허용해 무결성을 DB가 보장하지 못한다.
- **`nullable = false`만 적용** — INSERT에 FK가 들어가고 NOT NULL도 보장되지만, 테스트에서 FK UPDATE가 여전히 남았다. `updatable = false`까지 줘야 의도한 SQL이 된다.
- **양방향 (`OrderItem`에 `@ManyToOne` + `mappedBy`)** — FK를 OrderItem이 직접 관리하므로 SQL은 가장 자연스럽다. 하지만 OrderItem이 Order를 참조하게 되어, 연관관계 편의 메서드로 양쪽을 맞춰야 하는 부담이 생기고 단방향 + 위 옵션으로 SQL 문제가 해결되므로 굳이 택할 이유가 없었다.

## 참고한 자료
- 학습 테스트: `OrderItemMappingTest` (StatementInspector로 SQL 캡처, 바인딩 로그 `org.hibernate.orm.jdbc.bind=trace`)
  https://vladmihalcea.com/the-best-way-to-map-a-onetomany-association-with-jpa-and-hibernate/