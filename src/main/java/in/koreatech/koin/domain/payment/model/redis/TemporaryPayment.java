package in.koreatech.koin.domain.payment.model.redis;

import static in.koreatech.koin.domain.order.order.model.OrderStatus.CONFIRMING;
import static in.koreatech.koin.domain.order.order.model.OrderType.DELIVERY;
import static in.koreatech.koin.domain.order.order.model.OrderType.TAKE_OUT;
import static in.koreatech.koin.global.code.ApiResponseCode.MISMATCH_TEMPORARY_PAYMENT;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import in.koreatech.koin.domain.order.order.model.Order;
import in.koreatech.koin.domain.order.order.model.OrderDelivery;
import in.koreatech.koin.domain.order.order.model.OrderTakeout;
import in.koreatech.koin.domain.order.order.model.OrderType;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.exception.CustomException;
import lombok.Getter;

@Getter
@RedisHash(value = "TemporaryPayment@")
public class TemporaryPayment {

    private static final Long CACHE_EXPIRE_SECOND = 60 * 10L;

    @Id
    private String pgOrderId;

    private Integer userId;

    private Integer orderableShopId;

    private String phoneNumber;

    private OrderType orderType;

    private String address;

    private String addressDetail;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String toOwner;

    private String toRider;

    private Boolean provideCutlery;

    private Integer totalProductPrice;

    private Integer deliveryFee;

    private Integer totalPrice;

    @TimeToLive
    private Long expiryTime;

    private LocalDateTime createdAt;

    private TemporaryPayment(
        String pgOrderId,
        Integer userId,
        Integer orderableShopId,
        String phoneNumber,
        OrderType orderType,
        String address,
        String addressDetail,
        BigDecimal longitude,
        BigDecimal latitude,
        String toOwner,
        String toRider,
        Boolean provideCutlery,
        Integer totalProductPrice,
        Integer deliveryFee,
        Integer totalPrice
    ) {
        this.pgOrderId = pgOrderId;
        this.userId = userId;
        this.orderableShopId = orderableShopId;
        this.phoneNumber = phoneNumber;
        this.orderType = orderType;
        this.address = address;
        this.addressDetail = addressDetail;
        this.longitude = longitude;
        this.latitude = latitude;
        this.toOwner = toOwner;
        this.toRider = toRider;
        this.provideCutlery = provideCutlery;
        this.totalProductPrice = totalProductPrice;
        this.deliveryFee = deliveryFee;
        this.totalPrice = totalPrice;
        this.expiryTime = CACHE_EXPIRE_SECOND;
        this.createdAt = LocalDateTime.now();
    }

    public static TemporaryPayment toDeliveryEntity(
        String pgOrderId,
        Integer userId,
        Integer orderableShopId,
        String phoneNumber,
        String address,
        String addressDetail,
        BigDecimal longitude,
        BigDecimal latitude,
        String toOwner,
        String toRider,
        Boolean provideCutlery,
        Integer totalProductPrice,
        Integer deliveryFee,
        Integer totalPrice
    ) {
        return new TemporaryPayment(
            pgOrderId,
            userId,
            orderableShopId,
            phoneNumber,
            DELIVERY,
            address,
            addressDetail,
            longitude,
            latitude,
            toOwner,
            toRider,
            provideCutlery,
            totalProductPrice,
            deliveryFee,
            totalPrice
        );
    }

    public static TemporaryPayment toTakeOutEntity(
        String pgOrderId,
        Integer userId,
        Integer orderableShopId,
        String phoneNumber,
        String toOwner,
        Boolean provideCutlery,
        Integer totalProductPrice,
        Integer totalPrice
    ) {
        return new TemporaryPayment(
            pgOrderId,
            userId,
            orderableShopId,
            phoneNumber,
            TAKE_OUT,
            null,
            null,
            null,
            null,
            toOwner,
            null,
            provideCutlery,
            totalProductPrice,
            null,
            totalPrice
        );
    }

    public Order toOrder(User user, OrderableShop orderableShop) {
        Order order = Order.builder()
            .pgOrderId(pgOrderId)
            .orderType(orderType)
            .status(CONFIRMING)
            .orderableShopAddress(orderableShop.getShop().getAddress())
            .orderableShopAddressDetail(orderableShop.getShop().getAddressDetail())
            .orderableShopName(orderableShop.getShop().getName())
            .phoneNumber(phoneNumber)
            .totalProductPrice(totalProductPrice)
            .discountAmount(0) // 현재 할인 정책이 없기 때문에 0원 처리
            .totalPrice(totalPrice)
            .orderableShop(orderableShop)
            .user(user)
            .isDeleted(false)
            .build();

        if (orderType == DELIVERY) {
            order.setOrderDelivery(OrderDelivery.builder()
                .order(order)
                .address(address)
                .addressDetail(addressDetail)
                .latitude(latitude)
                .longitude(longitude)
                .toOwner(toOwner)
                .toRider(toRider)
                .provideCutlery(provideCutlery)
                .deliveryTip(deliveryFee)
                .build());
        } else if (orderType == TAKE_OUT) {
            order.setOrderTakeout(OrderTakeout.builder()
                .order(order)
                .toOwner(toOwner)
                .provideCutlery(provideCutlery)
                .build());
        }

        return order;
    }

    public void validateMatches(String pgOrderId, Integer userId, Integer amount) {
        validatePgOrderIdMatches(pgOrderId);
        validateUserIdMatches(userId);
        validateAmountMatches(amount);
    }

    private void validatePgOrderIdMatches(String pgOrderId) {
        if (!pgOrderId.equals(this.pgOrderId)) {
            throw CustomException.of(MISMATCH_TEMPORARY_PAYMENT);
        }
    }

    private void validateUserIdMatches(Integer userId) {
        if (!userId.equals(this.userId)) {
            throw CustomException.of(MISMATCH_TEMPORARY_PAYMENT);
        }
    }

    private void validateAmountMatches(Integer amount) {
        if (!amount.equals(this.totalPrice)) {
            throw CustomException.of(MISMATCH_TEMPORARY_PAYMENT);
        }
    }
}
