package github.sangwook.ecommerce.payment.exception;

import lombok.Getter;

@Getter
public enum LocalFailureReasonCode {

    CONNECTION_REFUSED("LOCAL_CONNECTION_REFUSED", "PG 서버 연결 실패 (다운 또는 네트워크 단절 의심)"),
    CONNECTION_POOL_EXHAUSTED("LOCAL_CONNECTION_POOL_EXHAUSTED", "커넥션 풀 고갈로 요청 전송 실패"),
    RESPONSE_DELAYED("LOCAL_RESPONSE_DELAYED", "응답 지연으로 처리 결과 확인 불가"),
    CONNECTION_ABORTED("LOCAL_CONNECTION_ABORTED", "연결이 예기치 않게 종료됨"),
    UNCLASSIFIED_IO_ERROR("LOCAL_UNCLASSIFIED_IO_ERROR", "분류되지 않은 I/O 오류");

    private final String code;
    private final String message;

    LocalFailureReasonCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}