package in.koreatech.koin.domain.timetableV3.dto.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetableV3.model.SemesterV3;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record SemesterCheckResponseV3(
    @Schema(description = "유저 id", example = "1", requiredMode = REQUIRED)
    Integer userId,

    @Schema(description = "유저 학기", requiredMode = REQUIRED)
    List<InnerSemesterCheckResponse> semesters
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerSemesterCheckResponse(
        @Schema(description = "year", example = "2024", requiredMode = REQUIRED)
        Integer year,

        @Schema(description = "term", example = "1학기", requiredMode = REQUIRED)
        String term
    ) {
        public static InnerSemesterCheckResponse of(SemesterV3 semesterV3) {
            return new InnerSemesterCheckResponse(
                semesterV3.getYear(),
                semesterV3.getTerm().getDescription()
            );
        }
    }

    public static SemesterCheckResponseV3 of(Integer userId, List<SemesterV3> semesterV3s) {
        return new SemesterCheckResponseV3(
            userId,
            semesterV3s.stream()
                .map(InnerSemesterCheckResponse::of)
                .toList()
        );
    }
}
