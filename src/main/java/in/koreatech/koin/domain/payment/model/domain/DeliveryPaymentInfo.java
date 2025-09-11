package in.koreatech.koin.domain.payment.model.domain;

import static in.koreatech.koin.global.code.ApiResponseCode.ORDER_PRICE_MISMATCH;

import java.math.BigDecimal;

import in.koreatech.koin.global.exception.CustomException;

public record DeliveryPaymentInfo(
    String phoneNumber,
    String address,
    String addressDetail,
    BigDecimal longitude,
    BigDecimal latitude,
    String toOwner,
    String toRider,
    Boolean provideCutlery,
    Integer totalMenuPrice,
    Integer deliveryTip,
    Integer totalAmount
) {
    public static DeliveryPaymentInfo of(
        String phoneNumber,
        String address,
        String addressDetail,
        BigDecimal longitude,
        BigDecimal latitude,
        String toOwner,
        String toRider,
        Boolean provideCutlery,
        Integer totalMenuPrice,
        Integer deliveryTip,
        Integer totalAmount
    ) {
        return new DeliveryPaymentInfo(
            phoneNumber,
            address,
            addressDetail,
            longitude,
            latitude,
            toOwner,
            toRider,
            provideCutlery,
            totalMenuPrice,
            deliveryTip,
            totalAmount
        );
    }

    public void validatePrice(Integer totalProductPrice, Integer deliveryFee, Integer finalAmount) {
        if (!totalMenuPrice().equals(totalProductPrice)
            || !deliveryTip().equals(deliveryFee)
            || !totalAmount().equals(finalAmount)
        ) {
            throw CustomException.of(ORDER_PRICE_MISMATCH);
        }
    }
}
