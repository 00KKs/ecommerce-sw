# ADR-0012: initiate 실패에서 retryable 구분을 제거

## 상태
채택

## 배경 (이걸 왜 고민했고, 어떤 상황이었는가)
결제 결과는 SUCCESS / FAILED / UNKNOWN처럼 sealed interface + switch로 구분하고 있고(ADR-0008), 실패 중에는 다시 시도하면 성공할 수 있는 "재시도 가능한 실패"가 존재한다. 기존에는 이 재시도 가능 여부를 initiate·confirm 양쪽 모두 `FAILED`의 `boolean retryable` 플래그로 표현했다.

      // initiate
      PaymentInitiateResult.FAILED(code, message, retryable)
      // confirm
      PaymentConfirmResult.FAILED(code, message, retryable)

그런데 `retryable = true`가 "누가 재시도하는가"를 명확히 하지 않은 채로 두 단계에 똑같이 붙어 있었다. initiate가 retryable=true로 실패하면 `PlaceOrderUseCase`가 주문을 PENDING_CONFIRMATION으로 두고 재고를 보류했지만, **정작 그 재시도를 수행하는 주체는 어디에도 없었다.**

실제 토스페이먼츠 흐름을 다시 보면 경계가 분명하다.

      [클라이언트 SDK]  결제창 인증 → paymentKey 획득      ← 서버 관여 없음
              │  paymentKey 전달
              ▼
      [서버]  confirm(paymentKey) → PG 호출               ← 서버가 실제로 호출하는 유일한 지점

initiate(결제창 인증)는 paymentKey를 **만드는** 단계로, 실제 흐름에서는 클라이언트 SDK가 수행하고 서버는 호출하지 않는다. 현재 구조는 클라이언트가 없어 주문 서버가 Fake PG의 인증 API를 대신 호출하고 있을 뿐이며, 실제 연동 시 이 호출은 제거된다(ADR-0011).

즉 **retryable(서버가 같은 요청을 자동으로 다시 보내 성공을 노리는 것)은 서버가 직접 호출하는 단계에서만 성립하는 개념**인데, 서버가 직접 호출하는 PG 단계는 confirm 하나뿐이다. initiate 실패는 클라이언트가 SDK에서 다시 인증해야 하는 일이므로, 서버 입장에서 "재시도 가능한 initiate 실패"라는 것은 존재하지
않는다.

## 결정
initiate 결과에서 retryable 구분을 제거하고 `SUCCESS | FAILED | UNKNOWN` 세 가지로만 둔다. 재시도 가능한 실패(RETRYABLE_FAILED)는 confirm에만 둔다.

      // initiate: retryable 개념 없음
      PaymentInitiateResult = SUCCESS | FAILED | UNKNOWN

      // confirm: 재시도 가능한 실패를 독립 케이스로 승격
      PaymentConfirmResult  = SUCCESS | FAILED | RETRYABLE_FAILED | UNKNOWN

- `PaymentInitiateResult.FAILED`에서 `boolean retryable`을 제거한다. initiate FAILED는 항상 종료(terminal) 상태로 다룬다 → 주문 실패 처리 및 재고 복원.
- initiate가 재시도되어야 할 상황(일시적 오류 등)도 서버 관점에서는 FAILED로 수렴시킨다. 재시도는 클라이언트가 SDK로 다시 시작하는 몫이다.
- 이에 따라 initiate의 재시도 가능 실패를 흉내 내던 테스트 시나리오(`InitiateScenario.RETRYABLE_FAILURE`)와 해당 통합 테스트 케이스는 제거하거나 일반 실패로 흡수한다.

## 이유
- **"서버가 재시도할 수 있는가"라는 경계와 타입이 일치한다.** 서버가 직접 호출하는 단계는 confirm뿐이므로, 서버 자동 재시도 대상인 retryable_fail도 confirm에만 두는 것이 흐름과 맞다.
- **주체 없는 재시도 상태를 없앤다.** 기존 initiate retryable=true는 주문을 보류시켜 놓고 아무도 재시도하지 않는 "떠 있는" 상태를 만들었다. 이를 제거해 재고가 무기한 묶이는 경로를 없앤다.
- **실제 토스 흐름으로 전환하기 쉬워진다.** 실제 연동 시 initiate 호출 자체가 사라지므로, initiate에 서버 재시도 의미를 부여하지 않는 편이 이후 제거를 단순하게 만든다(ADR-0011과 같은 방향).
- **모델이 단순해진다.** initiate는 성공/실패/결과불명만 구분하면 충분하고, 재시도 여부라는 축은 confirm에만 있으면 된다. 불필요한 플래그를 제거해 각 단계가 표현해야 할 상태를 최소화한다.
