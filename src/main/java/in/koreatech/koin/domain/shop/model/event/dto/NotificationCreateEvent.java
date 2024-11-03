package in.koreatech.koin.domain.shop.model.event.dto;

public record NotificationCreateEvent(
    Integer shopId,
    Integer studentId
) {

}
