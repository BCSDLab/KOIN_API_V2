package in.koreatech.koin.domain.order.shop.dto.menu;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenu;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuGroup;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuGroupMap;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * record 관련 레디스 캐시 직렬화 문제가 있어서 class 로 구현
 */
@Getter
@RequiredArgsConstructor
@JsonNaming(value = SnakeCaseStrategy.class)
public class OrderableShopMenusResponse {

    @Schema(description = "메뉴 그룹 ID", example = "1")
    private final Integer menuGroupId;

    @Schema(description = "메뉴 그룹 이름", example = "대표 메뉴")
    private final String menuGroupName;

    private final List<InnerMenuResponse> menus;

    public static OrderableShopMenusResponse from(OrderableShopMenuGroup menuGroup) {
        return new OrderableShopMenusResponse(menuGroup.getId(), menuGroup.getName(),
            InnerMenuResponse.from(menuGroup));
    }

    @Getter
    @RequiredArgsConstructor
    @JsonNaming(value = SnakeCaseStrategy.class)
    public static class InnerMenuResponse {

        @Schema(description = "메뉴 ID", example = "1")
        private final Integer id;

        @Schema(description = "메뉴 이름", example = "후라이드 치킨")
        private final String name;

        @Schema(description = "메뉴 설명", example = "바삭하고 고소한 오리지널 후라이드", nullable = true)
        private final String description;

        @Schema(description = "메뉴 썸네일 이미지 링크", example = "https://sample-image.com/chicken.jpg", nullable = true)
        private final String thumbnailImage;

        private final List<OrderableShopMenuPricesResponse> prices;

        private static List<InnerMenuResponse> from(OrderableShopMenuGroup menuGroup) {
            return menuGroup.getMenuGroupMap().stream().map(InnerMenuResponse::from).collect(Collectors.toList());
        }

        private static InnerMenuResponse from(OrderableShopMenuGroupMap innerMenuGroupMap) {
            OrderableShopMenu menu = innerMenuGroupMap.getMenu();
            return new InnerMenuResponse(menu.getId(), menu.getName(), menu.getDescription(), menu.getThumbnailImage(),
                OrderableShopMenuPricesResponse.from(menu));
        }
    }
}
