package in.koreatech.koin.domain.payment.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static in.koreatech.koin.domain.order.order.model.OrderType.DELIVERY;
import static in.koreatech.koin.domain.order.order.model.OrderType.TAKE_OUT;
import static in.koreatech.koin.domain.order.order.model.OrderStatus.CONFIRMING;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.order.model.Order;
import in.koreatech.koin.domain.order.order.model.OrderDelivery;
import in.koreatech.koin.domain.order.order.model.OrderMenu;
import in.koreatech.koin.domain.order.order.model.OrderMenuOption;
import in.koreatech.koin.domain.order.order.model.OrderTakeout;
import in.koreatech.koin.domain.payment.model.entity.Payment;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record PaymentResponse(
    @Schema(description = "결제 고유 id", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "주문 상점 고유 id", example = "1", requiredMode = REQUIRED)
    Integer orderableShopId,

    @Schema(description = "배달 주소", example = "충청남도 천안시 동남구 병천면 충절로 1600", requiredMode = NOT_REQUIRED)
    String deliveryAddress,

    @Schema(description = "배달상세 주소", example = "은솔관 422호", requiredMode = NOT_REQUIRED)
    String deliveryAddressDetails,

    @Schema(description = "가게 주소", example = "충청남도 천안시 동남구 병천면 충절로 1600 은솔관 422호", requiredMode = NOT_REQUIRED)
    String shopAddress,

    @Schema(description = "배달 주소 위도", example = "36.76125794", requiredMode = NOT_REQUIRED)
    BigDecimal longitude,

    @Schema(description = "배달 주소 경도", example = "127.28372942", requiredMode = NOT_REQUIRED)
    BigDecimal latitude,

    @Schema(description = "사장님에게", example = "리뷰 이벤트 감사합니다.", requiredMode = NOT_REQUIRED)
    String toOwner,

    @Schema(description = "라이더에게", example = "문 앞에 놔주세요.", requiredMode = NOT_REQUIRED)
    String toRider,

    @Schema(description = "수저, 포크 수령 여부", example = "true", requiredMode = REQUIRED)
    Boolean provideCutlery,

    @Schema(description = "메뉴 총 금액", example = "500", requiredMode = REQUIRED)
    Integer totalMenuPrice,

    @Schema(description = "배달비", example = "500", requiredMode = NOT_REQUIRED)
    Integer deliveryTip,

    @Schema(description = "결제 금액", example = "1000", requiredMode = REQUIRED)
    Integer amount,

    @Schema(description = "상점 이름", example = "굿모닝 살로만 치킨", requiredMode = REQUIRED)
    String shopName,

    @Schema(description = "주문 메뉴 목록", requiredMode = REQUIRED)
    List<InnerCartItemResponse> menus,

    @Schema(description = "주문 방법", example = "DELIVERY", requiredMode = REQUIRED)
    String orderType,

    @Schema(description = "간편결제사", example = "삼성페이", requiredMode = REQUIRED)
    String easyPayCompany,

    @Schema(description = "결제 요청 일시", example = "2025.06.21 21:00", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy.MM.dd HH:mm")
    LocalDateTime requestedAt,

    @Schema(description = "결제 승인 일시", example = "2025.06.21 21:00", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy.MM.dd HH:mm")
    LocalDateTime approvedAt,

    @Schema(description = "결제 수단", example = "카드", requiredMode = REQUIRED)
    String paymentMethod,

    @Schema(description = "예상 시각", example = "17:45", requiredMode = NOT_REQUIRED)
    @JsonFormat(pattern = "HH:mm")
    LocalTime estimatedAt,

    @Schema(description = "주문 상태", example = "COOKING", requiredMode = REQUIRED)
    String orderStatus
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerCartItemResponse(
        @Schema(description = "메뉴 이름", example = "허니콤보", requiredMode = REQUIRED)
        String name,

        @Schema(description = "수량", example = "1", requiredMode = REQUIRED)
        Integer quantity,

        @Schema(description = "메뉴 가격", example = "12300", requiredMode = REQUIRED)
        Integer price,

        @Schema(description = "선택한 옵션 목록", requiredMode = NOT_REQUIRED)
        List<InnerMenuOptionResponse> options
    ) {
        public static InnerCartItemResponse from(OrderMenu orderMenu) {
            List<InnerMenuOptionResponse> optionResponses = new ArrayList<>();

            if (orderMenu.getOrderMenuOptions() != null && !orderMenu.getOrderMenuOptions().isEmpty()) {
                optionResponses = orderMenu.getOrderMenuOptions().stream()
                    .map(InnerMenuOptionResponse::from)
                    .toList();
            }

            return new InnerCartItemResponse(
                orderMenu.getMenuName(),
                orderMenu.getQuantity(),
                orderMenu.getMenuPrice(),
                optionResponses
            );
        }
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerMenuOptionResponse(
        @Schema(description = "옵션 그룹 이름", example = "소스 추가", requiredMode = NOT_REQUIRED)
        String optionGroupName,

        @Schema(description = "옵션 이름", example = "레드디핑 소스", requiredMode = REQUIRED)
        String optionName,

        @Schema(description = "옵션 가격", example = "1000", requiredMode = REQUIRED)
        Integer optionPrice
    ) {
        public static InnerMenuOptionResponse from(OrderMenuOption orderMenuOption) {
            return new InnerMenuOptionResponse(
                orderMenuOption.getOptionGroupName(),
                orderMenuOption.getOptionName(),
                orderMenuOption.getOptionPrice()
            );
        }
    }

    private static Optional<LocalTime> computeEstimatedTime(Order order) {
        if (order.getStatus() == CONFIRMING) {
            return Optional.empty();
        }

        if (order.getOrderType() == DELIVERY) {
            OrderDelivery delivery = order.getOrderDelivery();
            if (delivery != null && delivery.getEstimatedArrivalAt() != null) {
                return Optional.of(LocalTime.from(delivery.getEstimatedArrivalAt()));
            }
        } else if (order.getOrderType() == TAKE_OUT) {
            OrderTakeout takeout = order.getOrderTakeout();
            if (takeout != null && takeout.getEstimatedPackagedAt() != null) {
                return Optional.of(LocalTime.from(takeout.getEstimatedPackagedAt()));
            }
        }

        return Optional.empty();
    }

    public static PaymentResponse of(
        Payment payment,
        Order order
    ) {
        OrderableShop orderableShop = order.getOrderableShop();
        Shop shop = orderableShop.getShop();

        String deliveryAddress = null;
        String deliveryAddressDetails = null;
        BigDecimal longitude = null;
        BigDecimal latitude = null;
        Integer deliveryTip = null;
        String toOwner = null;
        String toRider = null;
        Boolean provideCutlery = null;

        if (order.getOrderType() == DELIVERY) {
            OrderDelivery delivery = order.getOrderDelivery();
            deliveryAddress = delivery.getAddress();
            deliveryAddressDetails = delivery.getAddressDetail();
            longitude = delivery.getLongitude();
            latitude = delivery.getLatitude();
            deliveryTip = delivery.getDeliveryTip();
            toOwner = delivery.getToOwner();
            toRider = delivery.getToRider();
            provideCutlery = delivery.getProvideCutlery();
        } else if (order.getOrderType() == TAKE_OUT) {
            OrderTakeout takeout = order.getOrderTakeout();
            toOwner = takeout.getToOwner();
            provideCutlery = takeout.getProvideCutlery();
        }

        LocalTime estimatedTime = computeEstimatedTime(order).orElse(null);

        return new PaymentResponse(
            payment.getId(),
            orderableShop.getId(),
            deliveryAddress,
            deliveryAddressDetails,
            shop.getAddress(),
            longitude,
            latitude,
            toOwner,
            toRider,
            provideCutlery,
            order.getTotalProductPrice(),
            deliveryTip,
            order.getTotalPrice(),
            shop.getName(),
            order.getOrderMenus().stream()
                .map(InnerCartItemResponse::from)
                .toList(),
            order.getOrderType().name(),
            payment.getEasyPayCompany(),
            payment.getRequestedAt(),
            payment.getApprovedAt(),
            payment.getPaymentMethod().getDisplayName(),
            estimatedTime,
            order.getStatus().name()
        );
    }
}
