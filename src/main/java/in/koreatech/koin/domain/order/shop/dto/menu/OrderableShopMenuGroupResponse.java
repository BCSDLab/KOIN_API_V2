package in.koreatech.koin.domain.order.shop.dto.menu;

import java.util.List;

import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuGroup;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import io.swagger.v3.oas.annotations.media.Schema;

public record OrderableShopMenuGroupResponse(
    @Schema(description = "그룹 개수", example = "7")
    Integer count,
    @Schema(description = "메뉴 그룹")
    List<InnerMenuGroupResponse> menuGroups
) {

    private record InnerMenuGroupResponse(
        @Schema(description = "메뉴 그룹 ID")
        Integer id,
        @Schema(description = "메뉴 그룹 이름")
        String name
    ) {

        private static InnerMenuGroupResponse from(OrderableShopMenuGroup menuGroup) {
            return new InnerMenuGroupResponse(menuGroup.getId(), menuGroup.getName());
        }

        private static List<InnerMenuGroupResponse> from(List<OrderableShopMenuGroup> menuGroups) {
            return menuGroups.stream().map(InnerMenuGroupResponse::from).toList();
        }
    }

    public static OrderableShopMenuGroupResponse from(OrderableShop orderableShop) {
        List<OrderableShopMenuGroup> menuGroups = orderableShop.getMenuGroups();
        return new OrderableShopMenuGroupResponse(menuGroups.size(), InnerMenuGroupResponse.from(menuGroups));
    }
}
