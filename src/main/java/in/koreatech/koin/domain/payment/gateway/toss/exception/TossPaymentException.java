package in.koreatech.koin.domain.payment.gateway.toss.exception;

import org.springframework.util.StringUtils;

import lombok.Getter;

@Getter
public class TossPaymentException extends RuntimeException {

    private final TossPaymentErrorCode tossPaymentErrorCode;
    private final String detail;

    private TossPaymentException(TossPaymentErrorCode tossPaymentErrorCode, String detail) {
        super(tossPaymentErrorCode.getMessage());
        this.tossPaymentErrorCode = tossPaymentErrorCode;
        this.detail = detail;
    }

    private TossPaymentException(TossPaymentErrorCode tossPaymentErrorCode, String detail, Throwable cause) {
        super(tossPaymentErrorCode.getMessage(), cause);
        this.tossPaymentErrorCode = tossPaymentErrorCode;
        this.detail = detail;
    }

    public static TossPaymentException of(TossPaymentErrorCode tossPaymentErrorCode) {
        return new TossPaymentException(tossPaymentErrorCode, "");
    }

    public static TossPaymentException of(TossPaymentErrorCode tossPaymentErrorCode, String detail) {
        return new TossPaymentException(tossPaymentErrorCode, detail);
    }

    public static TossPaymentException of(TossPaymentErrorCode tossPaymentErrorCode, String detail, Throwable cause) {
        return new TossPaymentException(tossPaymentErrorCode, detail, cause);
    }

    public String getFullMessage() {
        if (StringUtils.hasText(detail)) {
            return super.getMessage();
        }
        return String.format("%s: %s", getMessage(), detail);
    }
}
