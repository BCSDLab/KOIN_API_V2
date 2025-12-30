package in.koreatech.koin.domain.course_registration.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record InnerLectureInfo(
    @Schema(description = "과목코드", example = "MEB302", requiredMode = REQUIRED)
    String lectureCode,

    @Schema(description = "과목명", example = "정역학", requiredMode = REQUIRED)
    String lectureName
) {
}
