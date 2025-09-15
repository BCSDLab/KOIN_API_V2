package in.koreatech.koin.domain.club.club.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import in.koreatech.koin.domain.club.club.model.ClubHot;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@JsonNaming(SnakeCaseStrategy.class)
public record ClubHotStatusResponse(
    @Schema(description = "해당 월", example = "7", requiredMode = REQUIRED)
    Integer month,

    @Schema(description = "해당 주차", example = "2", requiredMode = REQUIRED)
    Integer weekOfMonth,

    @Schema(description = "연속 인기 동아리 횟수", example = "3", requiredMode = REQUIRED)
    Integer streakCount
) {
    public static ClubHotStatusResponse from(ClubHot hot, int streakCount) {
        LocalDate start = hot.getStartDate();
        int month = start.getMonthValue();
        int weekOfMonth = ((start.getDayOfMonth() - 1) / 7) + 1;

        return new ClubHotStatusResponse(
            month,
            weekOfMonth,
            streakCount
        );
    }
}
