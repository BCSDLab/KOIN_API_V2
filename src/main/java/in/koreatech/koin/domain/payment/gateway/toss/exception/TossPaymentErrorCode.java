package in.koreatech.koin.domain.payment.gateway.toss.exception;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;

@Getter
public enum TossPaymentErrorCode {

    // 정의되지 않은 에러 코드인 경우 해당 에러코드를 내린다.
    UNKNOWN_ERROR("UNKNOWN_ERROR", "서버 에러가 발생했습니다. 관리자에게 문의해주세요.", 500);

    private final String code;
    private final String message;
    private final int statusCode;

    TossPaymentErrorCode(String code, String message, int statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private static final Map<String, TossPaymentErrorCode> ERROR_CODE_MAP =
        Stream.of(values()).collect(Collectors.toMap(TossPaymentErrorCode::getCode, e -> e));

    public static TossPaymentErrorCode fromCode(String code) {
        return ERROR_CODE_MAP.getOrDefault(code, UNKNOWN_ERROR);
    }
}
