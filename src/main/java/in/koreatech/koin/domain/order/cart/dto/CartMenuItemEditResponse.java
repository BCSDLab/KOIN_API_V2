package in.koreatech.koin.domain.order.cart.dto;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.cart.model.CartMenuItem;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenu;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuImage;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOption;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOptionGroup;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuPrice;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record CartMenuItemEditResponse(
    @Schema(description = "메뉴 고유 식별자", example = "1")
    Integer id,
    @Schema(description = "메뉴 이름", example = "후라이드 치킨")
    String name,
    @Schema(description = "메뉴 설명", example = "바삭하고 고소한 오리지널 후라이드", nullable = true)
    String description,
    @Schema(description = "메뉴 이미지 URL 목록", nullable = true)
    List<String> images,
    @Schema(description = "메뉴 가격 정보 목록")
    List<InnerPriceResponse> prices,
    @Schema(description = "메뉴 옵션 그룹 목록")
    List<InnerOptionGroupResponse> optionGroups
) {

    public static CartMenuItemEditResponse of(OrderableShopMenu menu, CartMenuItem cartMenuItem) {
        Integer selectedPriceId = cartMenuItem.getOrderableShopMenuPrice().getId();
        Set<Integer> selectedOptionIds = cartMenuItem.getCartMenuItemOptions().stream()
            .map(option -> option.getOrderableShopMenuOption().getId())
            .collect(Collectors.toSet());

        List<String> images = menu.getMenuImages()
            .stream()
            .map(OrderableShopMenuImage::getImageUrl)
            .toList();

        List<InnerPriceResponse> priceResponses = menu.getMenuPrices().stream()
            .map(price -> InnerPriceResponse.from(price, price.getId().equals(selectedPriceId)))
            .toList();

        List<InnerOptionGroupResponse> optionGroupResponses = menu.getMenuOptionGroupMap().stream()
            .map(groupMap -> InnerOptionGroupResponse.from(groupMap.getOptionGroup(), selectedOptionIds))
            .toList();

        return new CartMenuItemEditResponse(menu.getId(), menu.getName(), menu.getDescription(), images,
            priceResponses, optionGroupResponses);
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerOptionGroupResponse(
        @Schema(description = "옵션 그룹 고유 식별자", example = "1")
        Integer id,
        @Schema(description = "옵션 그룹 이름", example = "소스 추가")
        String name,
        @Schema(description = "옵션 그룹 설명", example = "다양한 소스를 추가해보세요!", nullable = true)
        String description,
        @Schema(description = "필수 선택 여부", example = "false")
        Boolean isRequired,
        @Schema(description = "최소 선택 가능 개수", example = "0")
        Integer minSelect,
        @Schema(description = "최대 선택 가능 개수", example = "3")
        Integer maxSelect,
        @Schema(description = "개별 옵션 목록")
        List<InnerOptionResponse> options
    ) {
        public static InnerOptionGroupResponse from(OrderableShopMenuOptionGroup group, Set<Integer> selectedOptionIds) {
            List<InnerOptionResponse> optionResponses = group.getMenuOptions().stream()
                .map(option -> InnerOptionResponse.from(option, selectedOptionIds.contains(option.getId())))
                .toList();

            return new InnerOptionGroupResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getIsRequired(),
                group.getMinSelect(),
                group.getMaxSelect(),
                optionResponses
            );
        }
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerOptionResponse(
        @Schema(description = "옵션 고유 식별자", example = "1")
        Integer id,
        @Schema(description = "옵션 이름", example = "양념 소스", nullable = true)
        String name,
        @Schema(description = "옵션 추가 가격", example = "500")
        Integer price,
        @Schema(description = "현재 장바구니 아이템에서 선택된 옵션인지 여부")
        Boolean isSelected
    ) {
        public static InnerOptionResponse from(OrderableShopMenuOption option, boolean isSelected) {
            return new InnerOptionResponse(option.getId(), option.getName(), option.getPrice(), isSelected);
        }
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerPriceResponse(
        @Schema(description = "메뉴 가격 옵션 고유 식별자", example = "1", nullable = true)
        Integer id,
        @Schema(description = "메뉴 가격 옵션 이름", example = "2마리", nullable = true)
        String name,
        @Schema(description = "메뉴 가격", example = "36000")
        Integer price,
        @Schema(description = "현재 선택된 가격 인지 여부")
        Boolean isSelected
    ) {
        public static InnerPriceResponse from(OrderableShopMenuPrice price, boolean isSelected) {
            return new InnerPriceResponse(price.getId(), price.getName(), price.getPrice(), isSelected);
        }
    }
}
