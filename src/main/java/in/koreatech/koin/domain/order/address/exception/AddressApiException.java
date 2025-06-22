package in.koreatech.koin.domain.order.address.exception;

import lombok.Getter;

@Getter
public class AddressApiException extends RuntimeException {

    private final AddressErrorCode errorCode;
    private final String detail;

    public AddressApiException(AddressErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = null;
    }

    public AddressApiException(AddressErrorCode errorCode, String detail) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = detail;
    }

    public String getFullMessage() {
        return detail != null ? String.format("%s", detail) : getMessage();
    }
}
