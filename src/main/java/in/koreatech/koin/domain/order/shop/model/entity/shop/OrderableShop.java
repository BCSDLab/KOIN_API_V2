package in.koreatech.koin.domain.order.shop.model.entity.shop;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Where;

import in.koreatech.koin.domain.order.shop.model.entity.delivery.OrderableShopDeliveryTip;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.global.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.order.shop.model.entity.delivery.OrderableShopDeliveryOption;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuGroup;
import in.koreatech.koin.domain.shop.model.event.EventArticle;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "orderable_shop")
@Where(clause = "is_deleted=0")
public class OrderableShop extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false, unique = true)
    private Shop shop;

    @Column(name = "delivery", nullable = false)
    private boolean delivery = true;

    @Column(name = "takeout", nullable = false)
    private boolean takeout = true;

    @Column(name = "service_event", nullable = false)
    private boolean serviceEvent = false;

    @Column(name = "minimum_order_amount", nullable = false)
    private Integer minimumOrderAmount;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @BatchSize(size = 7) // 기본 4개 (메인, 추천, 세트, 사이드) + 사장님 커스텀 3개
    @OneToMany(mappedBy = "orderableShop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderableShopMenuGroup> menuGroups = new ArrayList<>();

    @OneToMany(mappedBy = "orderableShop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderableShopImage> shopImages = new ArrayList<>();

    @OneToOne(cascade = {PERSIST, MERGE, REMOVE}, mappedBy = "orderableShop", fetch = FetchType.LAZY)
    private OrderableShopDeliveryOption deliveryOption;

    @OneToOne(cascade = {PERSIST, MERGE, REMOVE}, mappedBy = "orderableShop", fetch = FetchType.LAZY)
    private OrderableShopDeliveryTip deliveryTip;

    @Builder
    public OrderableShop(Integer id, Shop shop, boolean delivery, boolean takeout, boolean serviceEvent,
        Integer minimumOrderAmount, boolean isDeleted, List<OrderableShopMenuGroup> menuGroups) {
        this.id = id;
        this.shop = shop;
        this.delivery = delivery;
        this.takeout = takeout;
        this.serviceEvent = serviceEvent;
        this.minimumOrderAmount = minimumOrderAmount;
        this.isDeleted = isDeleted;
        this.menuGroups = menuGroups;
    }

    public void requireShopOpen() {
        if (shop == null || shop.getShopOperation() == null || !shop.getShopOperation().isOpen()) {
            throw CustomException.of(ApiResponseCode.SHOP_CLOSED);
        }
    }

    public Integer calculateDeliveryFee(Integer orderAmount) {
        return this.shop.getBaseDeliveryTips().calculateDeliveryTip(orderAmount);
    }

    public void requireMinimumOrderAmount(Integer totalOrderAmount) {
        if (totalOrderAmount < minimumOrderAmount) {
            throw CustomException.of(ApiResponseCode.ORDER_AMOUNT_BELOW_MINIMUM);
        }
    }

    public String getThumbnailImage() {
        return this.shopImages.stream()
            .filter(OrderableShopImage::getIsThumbnail)
            .map(OrderableShopImage::getImageUrl)
            .findFirst()
            .orElse(null);
    }

    public List<EventArticle> getOngoingEvent(Clock clock) {
        return this.shop.getEventArticles().stream()
            .filter(eventArticle -> eventArticle.isOngoing(clock))
            .collect(Collectors.toList());
    }

    public void updateDeliveryOption(OrderableShopDeliveryOption deliveryOption) {
        this.deliveryOption = deliveryOption;
    }

    public void updateDeliveryTip(OrderableShopDeliveryTip deliveryTip) {
        this.deliveryTip = deliveryTip;
    }

    public void updateOrderableShop(Integer minimumOrderAmount, boolean takeout) {
        if (minimumOrderAmount == null || minimumOrderAmount < 0) {
            throw CustomException.of(ApiResponseCode.INVAILID_MINIMUM_ORDER_AMOUNT);
        }
        this.minimumOrderAmount = minimumOrderAmount;
        this.takeout = takeout;
    }
}
