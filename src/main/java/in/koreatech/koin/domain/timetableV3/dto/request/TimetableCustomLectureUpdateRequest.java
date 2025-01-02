package in.koreatech.koin.domain.timetableV3.dto.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetableV3.model.TimetableLectureInformation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record TimetableCustomLectureUpdateRequest(
    @NotNull(message = "시간표 프레임 id를 입력해주세요.")
    @Schema(description = "시간표 프레임 id", example = "1004", requiredMode = REQUIRED)
    Integer timetableFrameId,

    @Valid
    @Schema(description = "커스텀 강의 정보", requiredMode = REQUIRED)
    @NotNull(message = "커스텀 강의 정보를 입력해주세요.")
    InnerTimeTableCustomLectureRequest timetableLecture
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerTimeTableCustomLectureRequest(
        @Schema(description = "시간표 id", example = "1", requiredMode = REQUIRED)
        @NotNull(message = "시간표 id를 입력해주세요.")
        Integer id,

        @Schema(description = "커스텀 강의 이름", example = "커스텀 강의 이름", requiredMode = NOT_REQUIRED)
        @Size(max = 100, message = "커스텀 강의 이름의 최대 글자는 100글자 입니다.")
        String classTitle,

        @Valid
        @Schema(description = "커스텀 강의 시간 정보", requiredMode = REQUIRED)
        List<LectureInfo> lectureInfos,

        @Schema(description = "교수명", example = "교수명", requiredMode = NOT_REQUIRED)
        @Size(max = 30, message = "교수명의 최대 글자는 30글자 입니다.")
        String professor
    ) {
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
    }

    public List<TimetableLectureInformation> toTimetableLectureInformations() {
        return timetableLecture.lectureInfos.stream()
            .map(lectureInfo -> TimetableLectureInformation.builder()
                .startTime(lectureInfo.startTime)
                .endTime(lectureInfo.endTime)
                .place(lectureInfo.place)
                .build()
            )
            .collect(Collectors.toList());
    }
}
