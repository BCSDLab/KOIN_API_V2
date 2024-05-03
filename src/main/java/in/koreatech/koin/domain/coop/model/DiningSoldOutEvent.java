package in.koreatech.koin.domain.coop.model;

import java.time.LocalDateTime;

import in.koreatech.koin.domain.dining.model.Dining;

public record DiningSoldOutEvent(
    Dining dining,
    LocalDateTime soldOutDateTime
) {

}
