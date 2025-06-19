package in.koreatech.koin.domain.order.cart.exception;

import lombok.Getter;

@Getter
public class CartException extends RuntimeException {

    private final CartErrorCode errorCode;
    private final String detail;

    public CartException(CartErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = null;
    }

    public CartException(CartErrorCode errorCode, String detail) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = detail;
    }

    public String getFullMessage() {
        return detail != null ? String.format("%s %s", getMessage(), detail) : getMessage();
    }
}
