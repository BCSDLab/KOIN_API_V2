package in.koreatech.koin.domain.order.shop.dto.menu;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenu;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuImage;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOption;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOptionGroup;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOptionGroupMap;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record OrderableShopMenuResponse(
    @Schema(description = "메뉴 고유 식별자", example = "1")
    Integer id,
    @Schema(description = "메뉴 이름", example = "후라이드 치킨")
    String name,
    @Schema(description = "메뉴 설명", example = "바삭하고 고소한 오리지널 후라이드", nullable = true)
    String description,
    @Schema(description = "메뉴 이미지 URL 목록", nullable = true)
    List<String> images,
    @Schema(description = "메뉴 가격 정보 목록")
    List<OrderableShopMenuPricesResponse> prices,
    @Schema(description = "메뉴 옵션 그룹 목록")
    List<InnerOrderableShopMenuOptionGroupResponse> optionGroups
) {

    public record InnerOrderableShopMenuOptionGroupResponse(
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
        List<InnerOrderableShopMenuOptionResponse> options
    ) {

        private static InnerOrderableShopMenuOptionGroupResponse from(
            OrderableShopMenuOptionGroupMap menuOptionGroupMap) {
            OrderableShopMenuOptionGroup optionGroup = menuOptionGroupMap.getOptionGroup();
            return new InnerOrderableShopMenuOptionGroupResponse(
                optionGroup.getId(),
                optionGroup.getName(),
                optionGroup.getDescription(),
                optionGroup.getIsRequired(),
                optionGroup.getMinSelect(),
                optionGroup.getMaxSelect(),
                InnerOrderableShopMenuOptionResponse.from(optionGroup.getMenuOptions())
            );
        }
    }

    public record InnerOrderableShopMenuOptionResponse(
        @Schema(description = "옵션 고유 식별자", example = "1")
        Integer id,
        @Schema(description = "옵션 이름", example = "양념 소스", nullable = true)
        String name,
        @Schema(description = "옵션 추가 가격", example = "500")
        Integer price
    ) {
        private static List<InnerOrderableShopMenuOptionResponse> from(List<OrderableShopMenuOption> menuOptions) {
            return menuOptions
                .stream()
                .map(InnerOrderableShopMenuOptionResponse::from)
                .toList();
        }

        private static InnerOrderableShopMenuOptionResponse from(OrderableShopMenuOption menuOption) {
            return new InnerOrderableShopMenuOptionResponse(menuOption.getId(), menuOption.getName(),
                menuOption.getPrice());
        }

    }

    public static OrderableShopMenuResponse from(OrderableShopMenu orderableShopMenu) {
        List<String> images = orderableShopMenu.getMenuImages()
            .stream()
            .map(OrderableShopMenuImage::getImageUrl)
            .toList();

        List<InnerOrderableShopMenuOptionGroupResponse> optionGroups = orderableShopMenu.getMenuOptionGroupMap()
            .stream()
            .map(InnerOrderableShopMenuOptionGroupResponse::from)
            .toList();

        return new OrderableShopMenuResponse(orderableShopMenu.getId(), orderableShopMenu.getName(),
            orderableShopMenu.getDescription(), images, OrderableShopMenuPricesResponse.from(orderableShopMenu), optionGroups);
    }
}
