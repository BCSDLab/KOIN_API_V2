package in.koreatech.koin.domain.order.cart.exception;

import org.springframework.http.HttpStatus;

import in.koreatech.koin._common.exception.errorcode.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CartErrorCode implements BaseErrorCode {
    DIFFERENT_SHOP_ITEM_IN_CART(HttpStatus.BAD_REQUEST, "장바구니에는 동일한 상점의 상품만 담을 수 있습니다."),
    MENU_SOLD_OUT(HttpStatus.BAD_REQUEST, "상품이 매진되었습니다"),
    MENU_PRICE_NOT_FOUND(HttpStatus.NOT_FOUND, "유효하지 않은 가격 ID 입니다."),
    MENU_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "유효하지 않은 옵션 ID 입니다."),
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니가 존재하지 않습니다"),
    CART_MENU_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니에 담긴 상품이 존재하지 않습니다"),
    INVALID_MENU_IN_SHOP(HttpStatus.BAD_REQUEST, "선택한 메뉴는 해당 상점에 속해있지 않습니다"),
    INVALID_OPTION_IN_GROUP(HttpStatus.BAD_REQUEST, "선택한 옵션이 해당 옵션 그룹에 속해있지 않습니다."),
    REQUIRED_OPTION_GROUP_MISSING(HttpStatus.BAD_REQUEST, "필수 옵션 그룹을 선택하지 않았습니다."),
    MIN_SELECTION_NOT_MET(HttpStatus.BAD_REQUEST, "옵션 그룹의 최소 선택 개수를 만족하지 못했습니다."),
    MAX_SELECTION_EXCEEDED(HttpStatus.BAD_REQUEST, "옵션 그룹의 최대 선택 개수를 초과했습니다."),
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "유효하지 않은 수량입니다.")
    ;

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
