package in.koreatech.koin.domain.timetableV3.dto.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import in.koreatech.koin.domain.timetableV3.model.LectureInformation;
import in.koreatech.koin.domain.timetableV3.model.TimetableRegularLectureInformation;
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

        @Schema(description = "강의 id", example = "3015", requiredMode = REQUIRED)
        @NotNull(message = "강의 id를 입력해주세요.")
        Integer lectureId,

        @Schema(description = "정규 강의 이름", example = "정규 강의 이름", requiredMode = NOT_REQUIRED)
        @Size(max = 100, message = "정규 강의 이름의 최대 글자는 100글자 입니다.")
        String classTitle,

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
    }
    public List<TimetableRegularLectureInformation> from(TimetableLecture timetableLecture, Lecture lecture) {
        List<LectureInformation> lectureInformations = lecture.getLectureInformations();
        List<TimetableRegularLectureInformation> response = new ArrayList<>();
        for (int index = 0; index < lectureInformations.size(); index++) {
            response.add(TimetableRegularLectureInformation.builder()
                .lectureInformation(lectureInformations.get(index))
                .timetableLecture(timetableLecture)
                .place(this.timetableLecture.classPlaces.get(index).classPlace)
                .build()
            );
        }
        return response;
    }
}
