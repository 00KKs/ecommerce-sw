# ADR-0002: Repository 인터페이스, 구현체 분리로 테스트 용이성 극대화

## 상태
채택 (2026-07-24)

## 배경 (이걸 왜 고민했고, 어떤 상황이었는가) 
회원과 배송지 규칙 대부분은 하나의 도메인 규칙을 지키는 것보다, 집합 규칙에 대한 내용이 많다.
이런 집합 규칙은 어플리케이션 서비스에 작성된다.
지금 이 회원-배송지 관계에서는 개별 도메인에 작성되는 규칙보다 서비스에 작성되는 규칙이 더 많기 때문에
각 도메인을 다루는 단위 테스트보다 서비스 로직 테스트가 중요하다고 판단했다.

하지만, 서비스는 Repository에 의존한다.
서비스 코드에 대한 테스트를 진행할 경우 데이터를 불러오는 Repository를 대체할 무언가가 필요했다.

여기서 먼저 언급해야할 것은 나중에 도메인이 복잡해질 것을 예상하여 JPA Entity와 도메인 객체의 분리를 생각하고 있었다.
이 상황에서 Service에 JPARepository를 상속받는 interface를 그대로 Service에 주입할 경우 엔티티 객체가 Service로 들어와 서비스가 영속 계층에 오염된다.
(Member 도메인은 가벼워서 JPA entity와 domain을 굳이 분리하여 사용하지 않았다.)

즉, 두 가지를 조건을 만족해야했다.
1. Service 테스트 용이성을 어떻게 향상시킬것인가
2. 나중에 영속 계층이 Service에 오염되지 않게 하려면 어떻게 만들어야할까

## 결정
Repository 추상화와 FakeRepository를 구현하여 테스트 코드 작성.

Repository 추상화를 선택했다.
Repository를 interface로 만들고 이를 구현하는 RepositoryImpl을 만들어, jpaRepository를 합성한다.
Service에서는 Repository interface만 의존하므로, 테스트 코드에서 FakeRepository를 구현체로 만들어 갈아끼워주기만 하면 된다.

## 이유
JPARepository를 그대로 Service에 의존하도록하면, JPARepository에 구현되어있는 메서드를 모두 override하여 재정의 해줘야한다.
이 문제를 해결하기 위해 필요한 interface만 정의해놓고 구현체에서 이를 구현하되 jpaRepository를 합성해서 사용한다.

## 결과
장점
- 테스트 용이성 극대화
- JPA Entity, domain 분리 시 Service를 순수하게 유지 가능

단점
- Service 테스트 코드 작성시마다 FakeRepository를 별도로 구현해줘야 하는 불편함 증가 
- JPA Entity와 domain을 분리해서 사용할 경우 한번 더 매핑해야하는 불편함 증가

작업량이 증가하는 불편함이 존재하지만, @SpringBootTest 어노테이션을 사용하지 않고 DB 없이 순수 Service 테스트를 진행할 수 있다는 점에서 부담이 줄어들었다.
또한, Repository 구현체를 마음대로 바꿀 수 있다는 점에서 유연성이 증가하였다.

## 검토한 대안들
- Mockito를 사용한 Repository를 Mock으로 대체한 테스트코드 작성
  - 현재 요구사항이 복잡하지 않아서 괜찮지만, Mock을 사용하면 repository 호출에 대해 어떻게 작동해야하는지 모두 적어줘야 하므로 작업량이 너무 증가할 것이라고 판단했다.
- @SpringBootTest로 통합 테스트
  - 순수 로직 테스트에는 과하다고 판단했다.
