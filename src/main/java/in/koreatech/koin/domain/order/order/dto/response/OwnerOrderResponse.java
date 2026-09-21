package in.koreatech.koin.domain.order.order.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.order.model.Order;
import in.koreatech.koin.domain.order.order.model.OrderDelivery;
import in.koreatech.koin.domain.order.order.model.OrderMenu;
import in.koreatech.koin.domain.order.order.model.OrderMenuOption;
import in.koreatech.koin.domain.payment.model.entity.Payment;
import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record OwnerOrderResponse(
    @Schema(description = "주문 고유 ID", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "주문 번호", example = "A1B2C3D4E5", requiredMode = REQUIRED)
    String orderNumber,

    @Schema(description = "주문 상태", example = "CONFIRMING", requiredMode = REQUIRED)
    String orderStatus,

    @Schema(description = "주문 접수 일시", example = "2026-09-20 18:42:00", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime orderedAt,

    @Schema(description = "주문 상품 목록", requiredMode = REQUIRED)
    List<InnerOrderMenuResponse> orderMenus,

    @Schema(description = "받는 사람 정보", requiredMode = REQUIRED)
    InnerReceiverResponse receiver,

    @Schema(description = "결제 정보", requiredMode = REQUIRED)
    InnerPaymentResponse payment,

    @Schema(description = "배달 완료 일시", example = "2026-09-20 19:21:00", requiredMode = NOT_REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime deliveredAt,

    @Schema(description = "반려 일시", example = "2026-09-20 18:33:00", requiredMode = NOT_REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime canceledAt,

    @Schema(description = "반려 사유", example = "재료 소진", requiredMode = NOT_REQUIRED)
    String canceledReason
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerOrderMenuResponse(
        @Schema(description = "주문 상품 고유 ID", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "상품 이름", example = "짜장면", requiredMode = REQUIRED)
        String menuName,

        @Schema(description = "선택한 가격 옵션 이름", example = "곱빼기", requiredMode = NOT_REQUIRED)
        String menuPriceName,

        @Schema(description = "상품 금액", example = "14000", requiredMode = REQUIRED)
        Integer menuPrice,

        @Schema(description = "수량", example = "2", requiredMode = REQUIRED)
        Integer quantity,

        @Schema(description = "선택한 옵션 목록", requiredMode = REQUIRED)
        List<InnerOrderMenuOptionResponse> options
    ) {
        public static InnerOrderMenuResponse from(OrderMenu orderMenu) {
            return new InnerOrderMenuResponse(
                orderMenu.getId(),
                orderMenu.getMenuName(),
                orderMenu.getMenuPriceName(),
                orderMenu.getMenuPrice(),
                orderMenu.getQuantity(),
                orderMenu.getOrderMenuOptions().stream()
                    .map(InnerOrderMenuOptionResponse::from)
                    .toList()
            );
        }
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerOrderMenuOptionResponse(
        @Schema(description = "옵션 그룹 이름", example = "추가 선택", requiredMode = REQUIRED)
        String optionGroupName,

        @Schema(description = "옵션 이름", example = "단무지 추가", requiredMode = REQUIRED)
        String optionName,

        @Schema(description = "옵션 금액", example = "500", requiredMode = REQUIRED)
        Integer optionPrice,

        @Schema(description = "옵션 수량", example = "1", requiredMode = REQUIRED)
        Integer quantity
    ) {
        public static InnerOrderMenuOptionResponse from(OrderMenuOption orderMenuOption) {
            return new InnerOrderMenuOptionResponse(
                orderMenuOption.getOptionGroupName(),
                orderMenuOption.getOptionName(),
                orderMenuOption.getOptionPrice(),
                orderMenuOption.getQuantity()
            );
        }
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerReceiverResponse(
        @Schema(description = "받는 사람 이름", example = "김민수", requiredMode = REQUIRED)
        String name,

        @Schema(description = "받는 사람 연락처", example = "01012341234", requiredMode = REQUIRED)
        String phoneNumber,

        @Schema(description = "받는 주소", example = "충청남도 천안시 동남구 병천면 충절로 1600", requiredMode = REQUIRED)
        String address,

        @Schema(description = "받는 상세 주소", example = "2공학관 201호", requiredMode = NOT_REQUIRED)
        String addressDetail,

        @Schema(description = "사장님에게 남긴 요청사항", example = "문 앞에 두고 벨 눌러주세요.", requiredMode = NOT_REQUIRED)
        String toOwner,

        @Schema(description = "수저 제공 여부", example = "true", requiredMode = REQUIRED)
        Boolean provideCutlery
    ) {
        public static InnerReceiverResponse of(Order order, User user) {
            OrderDelivery orderDelivery = order.getOrderDelivery();

            return new InnerReceiverResponse(
                user.getName(),
                order.getPhoneNumber(),
                orderDelivery.getAddress(),
                orderDelivery.getAddressDetail(),
                orderDelivery.getToOwner(),
                orderDelivery.getProvideCutlery()
            );
        }
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerPaymentResponse(
        @Schema(description = "결제 수단", example = "CARD", requiredMode = REQUIRED)
        String method,

        @Schema(description = "결제 일시", example = "2026-09-20 18:42:00", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime approvedAt,

        @Schema(description = "상품 금액", example = "32000", requiredMode = REQUIRED)
        Integer totalProductPrice,

        @Schema(description = "배달비", example = "3000", requiredMode = REQUIRED)
        Integer deliveryTip,

        @Schema(description = "할인 금액", example = "0", requiredMode = REQUIRED)
        Integer discountAmount,

        @Schema(description = "총 결제 금액", example = "35000", requiredMode = REQUIRED)
        Integer totalPrice
    ) {
        public static InnerPaymentResponse of(Order order, Payment payment) {
            return new InnerPaymentResponse(
                payment.getPaymentMethod().name(),
                payment.getApprovedAt(),
                order.getTotalProductPrice(),
                order.getOrderDelivery().getDeliveryTip(),
                order.getDiscountAmount(),
                order.getTotalPrice()
            );
        }
    }

    public static OwnerOrderResponse of(Order order, Payment payment) {
        return new OwnerOrderResponse(
            order.getId(),
            order.getOrderNumber(),
            order.getStatus().name(),
            order.getCreatedAt(),
            order.getOrderMenus().stream()
                .map(InnerOrderMenuResponse::from)
                .toList(),
            InnerReceiverResponse.of(order, order.getUser()),
            InnerPaymentResponse.of(order, payment),
            order.getOrderDelivery().getCompletedAt(),
            order.getCanceledAt(),
            order.getCanceledReason()
        );
    }
}
