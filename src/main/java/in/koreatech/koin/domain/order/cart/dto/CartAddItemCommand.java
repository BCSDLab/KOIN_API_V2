package in.koreatech.koin.domain.order.cart.dto;

import java.util.List;

public record CartAddItemCommand(
    Integer userId,
    Integer shopId,
    Integer shopMenuId,
    Integer shopMenuPriceId,
    List<Option> options,
    Integer quantity
) {

    public record Option(
        Integer optionGroupId,
        Integer optionId
    ) {

    }
}
