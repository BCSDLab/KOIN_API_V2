package in.koreatech.koin.domain.payment.model.entity;

import java.util.Arrays;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    CARD("카드"),
    VIRTUAL_ACCOUNT("가상계좌"),
    EASY_PAY("간편결제"),
    MOBILE_PHONE("휴대폰"),
    ACCOUNT_TRANSFER("계좌이체"),
    CULTURE_GIFT_CERTIFICATE("문화상품권"),
    BOOK_CULTURE_GIFT_CERTIFICATE("도서문화상품권"),
    GAME_CULTURE_GIFT_CERTIFICATE("게임문화상품권");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public static PaymentMethod from(String value) {
        return Arrays.stream(values())
            .filter(method -> method.displayName.equals(value))
            .findFirst()
            .orElse(null);
    }
}
