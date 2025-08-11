package in.koreatech.koin.domain.order.cart.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@JsonNaming(value = SnakeCaseStrategy.class)
public record CartAddItemRequest(
    @Schema(description = "주문 가능 상점 ID", example = "3", requiredMode = REQUIRED)
    @NotNull(message = "orderableShopId는 필수값입니다.")
    Integer orderableShopId,

    @Schema(description = "주문 가능 상점 메뉴 ID", example = "1", requiredMode = REQUIRED)
    @NotNull(message = "orderableShopMenuId는 필수값입니다.")
    Integer orderableShopMenuId,

    @Schema(description = "주문 가능 상점 메뉴 가격 ID", example = "1", requiredMode = REQUIRED)
    @NotNull(message = "orderableShopMenuPriceId는 필수값입니다.")
    Integer orderableShopMenuPriceId,

    @Schema(description = "주문 가능 상점 메뉴 옵션 가격 ID")
    List<InnerOptionRequest> orderableShopMenuOptionIds,

    @Schema(description = "메뉴 개수", example = "1", requiredMode = REQUIRED)
    @NotNull(message = "quantity는 필수값입니다.")
    @Min(value = 1, message = "수량은 최소 1 입니다.")
    @Max(value = 10, message = "수량은 최대 10 입니다.")
    Integer quantity
) {

    public CartAddItemCommand toCommand(Integer userId) {
        List<CartAddItemCommand.Option> options = orderableShopMenuOptionIds != null
            ? orderableShopMenuOptionIds.stream()
            .map(InnerOptionRequest::toOption)
            .collect(Collectors.toList())
            : List.of();

        return new CartAddItemCommand(userId, orderableShopId, orderableShopMenuId, orderableShopMenuPriceId,
            options, quantity);
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    private record InnerOptionRequest(
        @Schema(description = "옵션 그룹 ID", example = "1")
        Integer optionGroupId,
        @Schema(description = "옵션 ID", example = "2")
        Integer optionId
    ) {

        private CartAddItemCommand.Option toOption() {
            return new CartAddItemCommand.Option(optionGroupId, optionId);
        }
    }
}
