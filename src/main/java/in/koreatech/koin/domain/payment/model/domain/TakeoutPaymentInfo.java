package in.koreatech.koin.domain.payment.model.domain;

import static in.koreatech.koin.global.code.ApiResponseCode.ORDER_PRICE_MISMATCH;

import in.koreatech.koin.global.exception.CustomException;

public record TakeoutPaymentInfo(
    String phoneNumber,
    String toOwner,
    Boolean provideCutlery,
    Integer totalProductPrice,
    Integer totalAmount
) {
    public static TakeoutPaymentInfo of(
        String phoneNumber, String toOwner, Boolean provideCutlery,
        Integer totalMenuPrice, Integer totalAmount
    ) {
        return new TakeoutPaymentInfo(phoneNumber, toOwner, provideCutlery, totalMenuPrice, totalAmount);
    }

    public void validatePrice(Integer totalProductPrice, Integer finalAmount) {
        if (!totalProductPrice().equals(totalProductPrice) || !totalAmount().equals(finalAmount)) {
            throw CustomException.of(ORDER_PRICE_MISMATCH);
        }
    }
}
