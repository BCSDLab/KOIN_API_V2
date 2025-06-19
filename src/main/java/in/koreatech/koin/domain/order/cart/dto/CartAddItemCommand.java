package in.koreatech.koin.domain.order.cart.dto;

import java.util.List;

public record CartAddItemCommand(
    Integer userId,
    Integer shopId,
    Integer shopMenuId,
    Integer shopMenuPriceId,
    List<Option> options
) {

    public record Option(
        Integer optionGroupId,
        Integer optionId
    ) {

    }
}
