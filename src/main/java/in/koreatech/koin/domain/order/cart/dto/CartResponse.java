package in.koreatech.koin.domain.order.cart.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.cart.model.Cart;
import in.koreatech.koin.domain.order.cart.model.CartMenuItem;
import in.koreatech.koin.domain.order.cart.model.CartMenuItemOption;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuImage;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuPrice;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.model.shop.ShopImage;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record CartResponse(
    @Schema(description = "상점 이름", example = "굿모닝 살로만 치킨")
    String shopName,
    @Schema(description = "상점 이미지", example = "https://static.koreatech.in/test.png")
    String shopThumbnailImageUrl,
    @Schema(description = "주문 가능 상점 ID", example = "1")
    Integer orderableShopId,
    @Schema(description = "배달 가능 여부", example = "true")
    Boolean isDeliveryAvailable,
    @Schema(description = "포장 가능 여부", example = "true")
    Boolean isTakeoutAvailable,
    @Schema(description = "상점의 최소 주문 금액", example = "15000")
    Integer shopMinimumOrderAmount,
    @Schema(description = "장바구니에 담긴 상품 목록")
    List<InnerCartItemResponse> items,
    @Schema(description = "상품 총 금액 (배달비 제외)", example = "18000")
    Integer itemsAmount,
    @Schema(description = "배달비", example = "3500")
    Integer deliveryFee,
    @Schema(description = "최종 계산 금액 (상품 총 금액 + 배달비)", example = "20500")
    Integer totalAmount,
    @Schema(description = "결제 예정 금액 (할인 등을 반영한 최종 금액)", example = "28000")
    Integer finalPaymentAmount
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerCartItemResponse(
        @Schema(description = "장바구니 상품 고유 ID", example = "101")
        Integer cartMenuItemId,
        @Schema(description = "메뉴 이름", example = "허니콤보")
        String name,
        @Schema(description = "메뉴 썸네일 이미지", example = "https://static.koreatech.in/test.png")
        String menuThumbnailImageUrl,
        @Schema(description = "수량", example = "1")
        Integer quantity,
        @Schema(description = "해당 상품의 총 금액 (가격 * 수량)", example = "23000")
        Integer totalAmount,
        @Schema(description = "선택한 가격 정보")
        InnerPriceResponse price,
        @Schema(description = "선택한 옵션 목록")
        List<InnerMenuOptionResponse> options,
        @Schema(description = "담긴 상품의 정보(가격)가 장바구니 추가 이후 수정 되었는지 여부")
        Boolean isModified
    ) {

        public static InnerCartItemResponse from(CartMenuItem cartMenuItem) {
            List<InnerMenuOptionResponse> optionResponses = cartMenuItem.getCartMenuItemOptions().stream()
                .map(InnerMenuOptionResponse::from)
                .collect(Collectors.toList());

            return new InnerCartItemResponse(
                cartMenuItem.getId(),
                cartMenuItem.getOrderableShopMenu().getName(),
                cartMenuItem.getOrderableShopMenu().getThumbnailImage(),
                cartMenuItem.getQuantity(),
                cartMenuItem.calculateTotalAmount(),
                InnerPriceResponse.from(cartMenuItem.getOrderableShopMenuPrice()),
                optionResponses,
                cartMenuItem.getIsModified()
            );
        }
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerMenuOptionResponse(
        @Schema(description = "옵션 그룹 이름", example = "소스 추가")
        String optionGroupName,
        @Schema(description = "옵션 이름", example = "레드디핑 소스")
        String optionName,
        @Schema(description = "옵션 가격", example = "500")
        Integer optionPrice
    ) {
        public static InnerMenuOptionResponse from(CartMenuItemOption cartMenuItemOption) {
            String groupName = cartMenuItemOption.getOrderableShopMenuOption()
                .getOptionGroup()
                .getName();

            return new InnerMenuOptionResponse(
                groupName,
                cartMenuItemOption.getOptionName(),
                cartMenuItemOption.getOptionPrice()
            );
        }
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerPriceResponse(
        @Schema(description = "가격 옵션 이름", example = "순살", nullable = true)
        String name,
        @Schema(description = "가격", example = "23000")
        Integer price
    ) {
        public static InnerPriceResponse from(OrderableShopMenuPrice menuPrice) {
            return new InnerPriceResponse(menuPrice.getName(), menuPrice.getPrice());
        }
    }

    public static CartResponse from(Cart cart) {
        OrderableShop orderableShop = cart.getOrderableShop();
        Shop shop = orderableShop.getShop();

        List<InnerCartItemResponse> itemResponses = cart.getCartMenuItems().stream()
            .map(InnerCartItemResponse::from)
            .toList();

        int itemsAmount = cart.calculateItemsAmount();
        int deliveryFee = orderableShop.calculateDeliveryFee(itemsAmount);
        int totalAmount = itemsAmount + deliveryFee;
        int finalPaymentAmount = totalAmount; // 추후 쿠폰&적립금 등 할인 정책에 관한 요구 사항 추가 시 수정

        return new CartResponse(
            shop.getName(),
            orderableShop.getThumbnailImage(),
            orderableShop.getId(),
            orderableShop.isDelivery(),
            orderableShop.isTakeout(),
            orderableShop.getMinimumOrderAmount(),
            itemResponses,
            itemsAmount,
            deliveryFee,
            totalAmount,
            finalPaymentAmount
        );
    }

    public static CartResponse empty() {
        return new CartResponse(
            null,
            null,
            null,
            false,
            false,
            0,
            List.of(),
            0,
            0,
            0,
            0
        );
    }
}
