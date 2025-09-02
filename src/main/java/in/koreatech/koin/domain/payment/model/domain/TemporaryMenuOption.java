package in.koreatech.koin.domain.payment.model.domain;

public record TemporaryMenuOption(
    String optionGroupName,
    String optionName,
    Integer quantity,
    Integer optionPrice
) {

}
