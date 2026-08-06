package in.koreatech.koin.admin.lecture.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetable.model.Lecture;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminLectureCreateRequest(
    @Schema(description = "연도", example = "2026", requiredMode = REQUIRED)
    @NotNull(message = "연도는 필수입니다.")
    @Positive(message = "연도는 양수여야 합니다.")
    Integer year,

    @Schema(description = "학기", example = "1학기", requiredMode = REQUIRED)
    @NotBlank(message = "학기는 필수입니다.")
    String term,

    @Schema(description = "강의 정보 리스트", requiredMode = REQUIRED)
    @Valid
    @NotEmpty(message = "강의 정보 리스트는 비어 있을 수 없습니다.")
    List<LectureRequest> lectures
) {

    public List<Lecture> toEntities(String semester) {
        return lectures.stream()
            .map(lecture -> lecture.toEntity(semester))
            .toList();
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record LectureRequest(
        @Schema(description = "과목 코드", example = "ARB244", requiredMode = REQUIRED)
        @NotBlank(message = "과목 코드는 필수입니다.")
        @Size(max = 10, message = "과목 코드는 10자 이하여야 합니다.")
        String code,

        @Schema(description = "과목 이름", example = "건축구조의 이해 및 실습", requiredMode = REQUIRED)
        @NotBlank(message = "과목 이름은 필수입니다.")
        @Size(max = 50, message = "과목 이름은 50자 이하여야 합니다.")
        String name,

        @Schema(description = "대상 학년", example = "3", requiredMode = REQUIRED)
        @NotBlank(message = "대상 학년은 필수입니다.")
        @Size(max = 2, message = "대상 학년은 2자 이하여야 합니다.")
        String grades,

        @Schema(description = "분반", example = "01", requiredMode = REQUIRED)
        @NotBlank(message = "분반은 필수입니다.")
        @Size(max = 3, message = "분반은 3자 이하여야 합니다.")
        String lectureClass,

        @Schema(description = "수강 인원", example = "25", requiredMode = REQUIRED)
        @NotNull(message = "수강 인원은 필수입니다.")
        @Size(max = 4, message = "수강 인원은 4자 이하여야 합니다.")
        String regularNumber,

        @Schema(description = "학부", example = "디자인ㆍ건축공학부", requiredMode = REQUIRED)
        @NotBlank(message = "학부는 필수입니다.")
        @Size(max = 30, message = "학부는 30자 이하여야 합니다.")
        String department,

        @Schema(description = "대상", example = "디자 1 건축", requiredMode = REQUIRED)
        @NotNull(message = "대상은 필수입니다.")
        @Size(max = 200, message = "대상은 200자 이하여야 합니다.")
        String target,

        @Schema(description = "교수", example = "황현식", requiredMode = NOT_REQUIRED)
        @Size(max = 30, message = "교수명은 30자 이하여야 합니다.")
        String professor,

        @Schema(description = "영어 강의 여부", example = "N", requiredMode = REQUIRED)
        @NotNull(message = "영어 강의 여부는 필수입니다.")
        @Size(max = 2, message = "영어 강의 여부는 2자 이하여야 합니다.")
        String isEnglish,

        @Schema(description = "설계 학점", example = "0", requiredMode = REQUIRED)
        @NotBlank(message = "설계 학점은 필수입니다.")
        @Size(max = 2, message = "설계 학점은 2자 이하여야 합니다.")
        String designScore,

        @Schema(description = "이러닝 여부", example = "N", requiredMode = REQUIRED)
        @NotNull(message = "이러닝 여부는 필수입니다.")
        @Size(max = 2, message = "이러닝 여부는 2자 이하여야 합니다.")
        String isElearning,

        @Schema(description = "강의 시간", example = "[200, 201, 202, 203]", requiredMode = REQUIRED)
        @NotNull(message = "강의 시간은 필수입니다.")
        @Size(max = 50, message = "강의 시간은 최대 50개까지 입력할 수 있습니다.")
        List<@NotNull @PositiveOrZero @Max(999) Integer> classTime
    ) {

        public Lecture toEntity(String semester) {
            return Lecture.builder()
                .semester(semester)
                .code(code)
                .name(name)
                .grades(grades)
                .lectureClass(lectureClass)
                .regularNumber(regularNumber)
                .department(department)
                .target(target)
                .professor(professor)
                .isEnglish(isEnglish)
                .designScore(designScore)
                .isElearning(isElearning)
                .classTime(classTime.toString())
                .build();
        }
    }
}
