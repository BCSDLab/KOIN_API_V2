package in.koreatech.koin.domain.payment.gateway.toss.exception;

public class TossPaymentException extends RuntimeException {

    private final Integer statusCode;
    private final String errorCode;

    private TossPaymentException(String message, Integer statusCode, String errorCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public static TossPaymentException of(String message, Integer statusCode, String code) {
        return new TossPaymentException(message, statusCode, code);
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
