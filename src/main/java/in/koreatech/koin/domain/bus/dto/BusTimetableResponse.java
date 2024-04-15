package in.koreatech.koin.domain.bus.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.bus.model.BusTimetable;

@JsonNaming(SnakeCaseStrategy.class)
public record BusTimetableResponse(
    List<? extends BusTimetable> busTimetable,
    LocalDateTime updatedAt
) {
}
