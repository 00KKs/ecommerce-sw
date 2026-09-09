# ADR-0008: 결제 결과 구분에서 sealed interface와 switch-case문 사용

## 상태
채택

## 배경 (이걸 왜 고민했고, 어떤 상황이었는가)
결제 프로세스가 요청(request)과 승인(approve) 2단계로 나뉘면서, 실패가 발생하는 지점도 두 군데로 나뉘게 되었다.
요청 단계에서 실패하면 아직 PG로부터 paymentKey를 발급받지 못한 상태이고, 승인 단계에서 실패하면 이미 paymentKey는 존재하지만 PG가 승인을 거절한 상태다.
초기에는 `PaymentResult`를 `{ success: boolean, paymentKey: String, failReason: String }` 형태의 단일 클래스로 만들고 paymentKey를 nullable로 두는 방식을 고려했다.
하지만 이 경우 "성공인데 paymentKey가 null인 상태"나 "실패인데 어느 단계인지 모르는 상태" 같은, 도메인적으로 있을 수 없는 조합도 타입상으로는 만들어질 수 있어 컴파일러가 이를 막아주지 못했다.
`PlaceOrderUseCase`에서 이 결과에 따라 분기 처리(주문 확정 / 재고 복원 후 실패 처리)를 해야 했기 때문에, 호출부에서 실패를 놓치거나 필드 조합을 잘못 다루는 실수를 막을 방법이 필요했다.

## 결정
`PaymentResult`를 sealed interface로 정의하고, 성공/실패를 각각 별도의 record로 표현한다.

```java
sealed interface PaymentResult {
    record SUCCESS(String paymentKey) implements PaymentResult {}
    record PAYMENT_FAILED(PaymentFailedStage stage) implements PaymentResult {}
}

sealed interface PaymentFailedStage {
    record PAYMENT_INITIATE() implements PaymentFailedStage {}
    record PAYMENT_CONFIRM(String paymentKey) implements PaymentFailedStage {}
}
```

- `SUCCESS`는 paymentKey를 항상 가진다 (nullable 아님).
- `PAYMENT_FAILED`는 실패 단계(`PaymentFailedStage`)를 갖고, 그 안에서 다시 요청 단계 실패(`PAYMENT_INITIATE`, paymentKey 없음)와 승인 단계 실패(`PAYMENT_CONFIRM`, paymentKey 있음)로 나뉜다.
- 호출부(`PlaceOrderUseCase`)에서는 `switch` 패턴 매칭으로 각 case를 처리하며, sealed interface이므로 컴파일러가 모든 분기 처리를 강제한다. 새로운 실패 단계가 추가되면 switch문에서 컴파일 에러가 발생해 처리 누락을 방지한다.

## 이유
- paymentKey의 유무가 "우연히 null일 수도 있는 값"이 아니라 "어느 상태에서는 존재 자체가 불가능한 값"이라는 도메인 사실을 타입으로 표현할 수 있다.
- boolean + nullable 필드 조합 대비, 있을 수 없는 상태 조합(예: 성공인데 키가 없음)을 컴파일 타임에 원천 차단한다.
- sealed interface의 특징인 switch exhaustiveness(switch 철저성) 덕분에, 나중에 실패 단계가 하나 더 추가되어도(예: PG 조회 타임아웃) 해당 분기를 빠뜨린 채 배포되는 사고를 막을 수 있다.
  - default 문이 필요없고, 단계가 하나라도 빠지면 아예 컴파일이 안됨.
- 호출부 코드가 곧 상태 전이 다이어그램 역할을 해서, 결제 흐름을 별도 문서 없이도 코드만 보고 파악하기 쉬워진다.

## 검토한 대안들
- **단일 클래스 + nullable 필드** (`success`, `paymentKey`, `failReason`) — 구현은 간단하지만, 필드 조합에 대한 유효성 검증을 런타임 로직(if문)에 의존해야 하고, 잘못된 조합이 생성돼도 컴파일러가 잡아주지 못함.
- **enum 상태값 + 별도 DTO** — 상태는 명확히 표현되지만, 상태별로 필요한 데이터(paymentKey 유무)가 여전히 하나의 DTO에 옵셔널 필드로 뭉쳐 있어 근본적인 문제가 해결되지 않음.
- **예외(Exception) 기반 처리** — 요청 단계 실패와 승인 단계 실패를 서로 다른 예외 타입으로 던지는 방식도 고려했으나, 결제 실패는 시스템 오류가 아니라 정상적으로 발생 가능한 비즈니스 흐름이라 예외보다는 반환 타입으로 표현하는 것이 더 적합하다고 판단.