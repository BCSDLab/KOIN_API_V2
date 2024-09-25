package in.koreatech.koin.domain.shop.repository.shop.dto;

import java.time.LocalTime;

public record DurationTime(
    LocalTime start,
    LocalTime end
) {
}
