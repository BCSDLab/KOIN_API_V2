package in.koreatech.koin.domain.order.order.util;

import static in.koreatech.koin.global.code.ApiResponseCode.ORDER_PRICE_MISMATCH;
import static lombok.AccessLevel.PRIVATE;

import in.koreatech.koin.global.exception.CustomException;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class OrderPriceValidator {

    public static void validatePrice(
        int requestedMenuPrice,
        int requestedTotalAmount,
        int totalProductPrice,
        int totalPrice
    ) {
        validatePrice(requestedMenuPrice, 0, requestedTotalAmount, totalProductPrice, 0, totalPrice);
    }

    public static void validatePrice(
        int requestedMenuPrice,
        int requestedDeliveryTip,
        int requestedTotalAmount,
        int totalProductPrice,
        int deliveryFee,
        int totalPrice
    ) {
        if (requestedMenuPrice != totalProductPrice || requestedDeliveryTip != deliveryFee || requestedTotalAmount != totalPrice) {
            throw CustomException.of(ORDER_PRICE_MISMATCH);
        }
    }
}
