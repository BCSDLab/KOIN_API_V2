package in.koreatech.koin.domain.timetableV3.dto.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
        @NotEmpty(message = "강의 시간 정보는 필수로 입력해야 합니다.")
        @Schema(description = "커스텀 강의 시간 정보", requiredMode = REQUIRED)
        List<LectureInfoRequest> lectureInfos,

        @Schema(description = "교수명", example = "교수명", requiredMode = NOT_REQUIRED)
        @Size(max = 30, message = "교수명의 최대 글자는 30글자 입니다.")
        String professor
    ) {

        // 커스텀 강의 장소 역정규화
        public String joinClassPlaces() {
            return lectureInfos.stream()
                .map(LectureInfoRequest::place)
                .collect(Collectors.joining(", "));
        }

        // 커스텀 강의 시간 역정규화
        public String joinClassTimes() {
            List<Integer> classTimes = new ArrayList<>();

            for (int index = 0; index < lectureInfos.size(); index++) {
                if (index > 0) classTimes.add(-1);

                int startTime = lectureInfos.get(index).startTime();
                int endTime = lectureInfos.get(index).endTime();

                while (startTime <= endTime) {
                    classTimes.add(startTime++);
                }
            }
            return classTimes.toString();
        }
    }

}
