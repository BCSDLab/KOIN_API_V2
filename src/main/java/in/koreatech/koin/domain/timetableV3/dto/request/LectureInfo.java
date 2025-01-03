package in.koreatech.koin.domain.timetableV3.dto.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record LectureInfo(
    @Schema(description = "시작 시간", example = "112", requiredMode = REQUIRED)
    Integer startTime,

    @Schema(description = "종료 시간", example = "115", requiredMode = REQUIRED)
    Integer endTime,

    @Schema(description = "장소", example = "2공학관314", requiredMode = NOT_REQUIRED)
    @Size(max = 30, message = "강의 장소의 최대 글자는 30글자입니다.")
    String place
) {

}
