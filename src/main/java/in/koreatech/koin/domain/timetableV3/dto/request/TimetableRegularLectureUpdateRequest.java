package in.koreatech.koin.domain.timetableV3.dto.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record TimetableRegularLectureUpdateRequest(
    @NotNull(message = "시간표 프레임 id를 입력해주세요.")
    @Schema(description = "시간표 프레임 id", example = "1004", requiredMode = REQUIRED)
    Integer timetableFrameId,

    @Valid
    @Schema(description = "정규 강의 정보", requiredMode = REQUIRED)
    @NotNull(message = "정규 강의 정보를 입력해주세요.")
    InnerTimeTableRegularLectureRequest timetableLecture
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerTimeTableRegularLectureRequest(
        @Schema(description = "시간표 id", example = "1", requiredMode = REQUIRED)
        @NotNull(message = "시간표 id를 입력해주세요.")
        Integer id,

        @Schema(description = "강의 id", example = "3015", requiredMode = NOT_REQUIRED)
        Integer lectureId,

        @Schema(description = "정규 강의 이름", example = "정규 강의 이름", requiredMode = NOT_REQUIRED)
        @Size(max = 100, message = "정규 강의 이름의 최대 글자는 100글자 입니다.")
        String classTitle,

        @Schema(description = "이수 구분", example = "교양선택", requiredMode = NOT_REQUIRED)
        String courseType,

        @Schema(description = "교양 이수 구분", example = "자연과인간", requiredMode = NOT_REQUIRED)
        String generalEducationArea,

        @Valid
        @Schema(description = "정규 강의 장소 정보", requiredMode = REQUIRED)
        List<ClassPlace> classPlaces
    ) {
        @JsonNaming(value = SnakeCaseStrategy.class)
        public record ClassPlace(
            @Schema(description = "장소", example = "2공학관314", requiredMode = NOT_REQUIRED)
            @Size(max = 30, message = "강의 장소의 최대 글자는 30글자입니다.")
            @NotNull(message = "강의 장소를 입력해주세요.")
            String classPlace
        ) {

        }

        // 정규 강의 장소 역정규화 메소드
        public String classPlacesToString() {
            return classPlaces.stream()
                .map(ClassPlace::classPlace)
                .collect(Collectors.joining(", "));
        }
    }
}
