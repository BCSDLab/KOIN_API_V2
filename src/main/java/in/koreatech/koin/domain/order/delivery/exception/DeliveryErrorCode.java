package in.koreatech.koin.domain.order.delivery.exception;

import org.springframework.http.HttpStatus;

import in.koreatech.koin._common.exception.errorcode.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DeliveryErrorCode implements BaseErrorCode {

    // 400 (사용자 입력 오류)
    INVALID_DELIVERY_AREA(HttpStatus.BAD_REQUEST, "배달이 불가능한 지역이에요."),
    // 404
    CAMPUS_DELIVERY_ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "교내 배달 주소를 찾을 수 없습니다.");

    private final HttpStatus httpStatusCode;
    @Getter
    private final String message;

    @Override
    public String getHttpCode() {
        return httpStatusCode.toString();
    }

    @Override
    public Integer getHttpIntegerCode() {
        return httpStatusCode.value();
    }

}
