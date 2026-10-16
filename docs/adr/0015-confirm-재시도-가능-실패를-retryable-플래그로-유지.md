# ADR-0015: confirm의 재시도 가능 실패를 독립 케이스 대신 retryable 플래그로 유지

## 상태
채택 (ADR-0012의 confirm 관련 결정 일부를 대체)

## 배경 (이걸 왜 고민했고, 어떤 상황이었는가)
ADR-0012에서는 initiate의 retryable을 제거하면서, confirm의 재시도 가능 실패는
`PaymentConfirmResult = SUCCESS | FAILED | RETRYABLE_FAILED | UNKNOWN`처럼 독립 케이스로 승격하기로 했다.

구현 과정에서 재시도 가능 실패가 어디까지 전파되는지 다시 확인했다.
- 재시도는 `MyPaymentGateway.confirmPayment` 안에서 Resilience4j Retry로 수행한다(0.5s→1s→2s, 최대 3회).
- 재시도를 소진하면 Gateway가 retryable=false인 FAILED로 바꿔 반환한다.
- 즉 "재시도 가능한 실패"는 Gateway 내부의 재시도 판단에만 쓰이고, Gateway 밖으로 나가지 않는다.

이 상태에서 RETRYABLE_FAILED를 공개 타입의 케이스로 추가하면, Gateway를 호출하는 `PaymentConfirmResolver`의
switch가 도달할 수 없는 케이스를 강제로 처리해야 한다.

## 결정
`PaymentConfirmResult.FAILED(reasonCode, reasonMessage, boolean retryable)`의 플래그 구조를 유지한다.
- retryable=true는 Gateway 내부에서 Retry의 `retryOnResult` 판단에만 사용한다.
- Gateway는 재시도를 소진하면 retryable=false로 바꿔 반환한다. Gateway 밖에서 retryable=true인 FAILED는 나오지 않는다.

## 이유
- 재시도의 주체와 범위가 Gateway 하나로 닫혀 있어, 바깥 계층이 재시도 가능 여부를 분기할 필요가 없다.
- 공개 타입에 도달하지 않는 케이스를 추가하면, sealed switch의 철저성이 오히려 의미 없는 분기를 만들어낸다.
- 마무리 단계에서 내부/공개 결과 타입을 분리하는 리팩터링 비용에 비해 얻는 이점이 작다고 판단했다.

## 검토한 대안들
- **RETRYABLE_FAILED를 공개 케이스로 승격 (ADR-0012 원안)** — 재시도 가능 여부가 타입에 드러나지만, Resolver에 도달하지 않는 분기가 생긴다.
- **Gateway 내부 재시도용 타입과 공개 타입을 분리** — 내부 타입(SUCCESS | FAILED | RETRYABLE_FAILED | UNKNOWN)으로 재시도하고, 밖으로는 RETRYABLE_FAILED가 없는 타입만 반환한다.

## 결과
- 이 때문에 `PaymentResultTranslator`의 `retryable=true → PENDING` 분기는 실제로 도달하지 않는다.

- Gateway는 재시도를 소진하면 retryable=false로 바꿔 반환한다. Gateway 밖에서 retryable=true인 FAILED는 나오지 않는다.

## 이유
- 재시도의 주체와 범위가 Gateway 하나로 닫혀 있어, 바깥 계층이 재시도 가능 여부를 분기할 필요가 없다.
- 공개 타입에 도달하지 않는 케이스를 추가하면, sealed switch의 철저성이 오히려 의미 없는 분기를 만들어낸다.
- 마무리 단계에서 내부/공개 결과 타입을 분리하는 리팩터링 비용에 비해 얻는 이점이 작다고 판단했다.

## 검토한 대안들
- **RETRYABLE_FAILED를 공개 케이스로 승격 (ADR-0012 원안)** — 재시도 가능 여부가 타입에 드러나지만, Resolver에 도달하지 않는 분기가 생긴다.
- **Gateway 내부 재시도용 타입과 공개 타입을 분리** — 내부 타입(SUCCESS | FAILED | RETRYABLE_FAILED | UNKNOWN)으로 재시도하고, 밖으로는 RETRYABLE_FAILED가 없는 타입만 반환한다.
  "있을 수 없는 상태를 타입으로 막는다"(ADR-0008)에 가장 부합하지만, 변환 계층이 하나 더 생긴다.

## 결과
- 타입상으로는 Gateway 밖에서 `FAILED(retryable=true)`를 만들 수 있다. 이를 막는 것은 타입이 아니라 Gateway 구현(재시도 소진 시 false로 변환)이다.
- 이 때문에 `PaymentResultTranslator`의 `retryable=true → PENDING` 분기는 실제로 도달하지 않는다.
- 향후 개선: 대안 2(내부/공개 타입 분리)를 적용하면 위 두 가지가 함께 해소된다.