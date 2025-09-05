package in.koreatech.koin.domain.payment.model.redis;

import static in.koreatech.koin.domain.order.model.OrderType.DELIVERY;
import static in.koreatech.koin.domain.order.model.OrderType.TAKE_OUT;
import static in.koreatech.koin.global.code.ApiResponseCode.MISMATCH_TEMPORARY_PAYMENT;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import in.koreatech.koin.domain.order.model.Order;
import in.koreatech.koin.domain.order.model.OrderDelivery;
import in.koreatech.koin.domain.order.model.OrderTakeout;
import in.koreatech.koin.domain.order.model.OrderType;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.payment.model.domain.TemporaryMenuItems;
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

    private String toOwner;

    private String toRider;

    private Boolean provideCutlery;

    private Integer totalProductPrice;

    private Integer deliveryFee;

    private Integer totalPrice;

    private List<TemporaryMenuItems> temporaryMenuItems;

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
        String toOwner,
        String toRider,
        Boolean provideCutlery,
        Integer totalProductPrice,
        Integer deliveryFee,
        Integer totalPrice,
        List<TemporaryMenuItems> temporaryMenuItems
    ) {
        this.pgOrderId = pgOrderId;
        this.userId = userId;
        this.orderableShopId = orderableShopId;
        this.phoneNumber = phoneNumber;
        this.orderType = orderType;
        this.address = address;
        this.addressDetail = addressDetail;
        this.toOwner = toOwner;
        this.toRider = toRider;
        this.provideCutlery = provideCutlery;
        this.totalProductPrice = totalProductPrice;
        this.deliveryFee = deliveryFee;
        this.totalPrice = totalPrice;
        this.temporaryMenuItems = temporaryMenuItems;
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
        String toOwner,
        String toRider,
        Boolean provideCutlery,
        Integer totalProductPrice,
        Integer deliveryFee,
        Integer totalPrice,
        List<TemporaryMenuItems> temporaryMenuItems
    ) {
        return new TemporaryPayment(
            pgOrderId,
            userId,
            orderableShopId,
            phoneNumber,
            DELIVERY,
            address,
            addressDetail,
            toOwner,
            toRider,
            provideCutlery,
            totalProductPrice,
            deliveryFee,
            totalPrice,
            temporaryMenuItems
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
        Integer totalPrice,
        List<TemporaryMenuItems> temporaryMenuItems
    ) {
        return new TemporaryPayment(
            pgOrderId,
            userId,
            orderableShopId,
            phoneNumber,
            TAKE_OUT,
            null,
            null,
            toOwner,
            null,
            provideCutlery,
            totalProductPrice,
            null,
            totalPrice,
            temporaryMenuItems
        );
    }

    public Order toOrder(User user, OrderableShop orderableShop) {
        Order order = Order.builder()
            .pgOrderId(pgOrderId)
            .orderType(orderType)
            .phoneNumber(phoneNumber)
            .totalProductPrice(totalProductPrice)
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
