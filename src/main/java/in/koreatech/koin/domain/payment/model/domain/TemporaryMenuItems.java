package in.koreatech.koin.domain.payment.model.domain;

import java.util.List;

public record TemporaryMenuItems(
    String name,
    Integer quantity,
    Integer totalAmount,
    TemporaryMenuPrice price,
    List<TemporaryMenuOption> options
) {

}
