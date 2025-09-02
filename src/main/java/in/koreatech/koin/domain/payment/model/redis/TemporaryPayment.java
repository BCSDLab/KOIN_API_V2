package in.koreatech.koin.domain.payment.model.redis;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import in.koreatech.koin.domain.order.model.OrderType;
import in.koreatech.koin.domain.payment.model.domain.TemporaryMenuItems;
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
            OrderType.DELIVERY,
            address,
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
            OrderType.TAKE_OUT,
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
}
