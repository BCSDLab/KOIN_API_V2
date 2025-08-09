package in.koreatech.koin.domain.order.cart.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.cart.dto.CartAddItemCommand.Option;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@JsonNaming(value = SnakeCaseStrategy.class)
public record CartUpdateItemRequest(
    @Schema(description = "메뉴 수량", example = "2")
    Integer quantity,

    @Schema(description = "새롭게 선택한 메뉴 가격 ID", example = "2", requiredMode = REQUIRED)
    @NotNull
    Integer orderableShopMenuPriceId,

    @Schema(description = "새롭게 선택한 옵션 목록")
    List<InnerOptionRequest> options
) {

    public List<Option> toOptions() {
        return options != null ?
            options.stream()
                .map(option -> new Option(option.optionGroupId(), option.optionId()))
                .toList()
            : List.of();
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerOptionRequest(
        @Schema(description = "옵션 그룹 ID", example = "1")
        Integer optionGroupId,

        @Schema(description = "옵션 ID", example = "2")
        Integer optionId
    ) {

        private Option toOption() {
            return new Option(optionGroupId, optionId);
        }
    }
}
