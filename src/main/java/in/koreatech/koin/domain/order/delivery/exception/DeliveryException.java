package in.koreatech.koin.domain.order.delivery.exception;

import lombok.Getter;

@Getter
public class DeliveryException extends RuntimeException {

    private final DeliveryErrorCode errorCode;
    private final String detail;

    public DeliveryException(DeliveryErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = null;
    }

    public DeliveryException(DeliveryErrorCode errorCode, String detail) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = detail;
    }

    public String getFullMessage() {
        return detail != null ? String.format("%s", detail) : getMessage();
    }
}
