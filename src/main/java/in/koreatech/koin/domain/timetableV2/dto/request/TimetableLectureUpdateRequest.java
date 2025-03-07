package in.koreatech.koin.domain.timetableV2.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record TimetableLectureUpdateRequest(
    @Schema(description = "시간표 프레임 id", example = "1", requiredMode = REQUIRED)
    @NotNull(message = "시간표 식별 번호를 입력해주세요.")
    Integer timetableFrameId,

    @Valid
    @NotEmpty(message = "시간표 정보를 입력해주세요.")
    @Schema(description = "시간표 정보", requiredMode = NOT_REQUIRED)
    List<InnerTimetableLectureRequest> timetableLecture
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerTimetableLectureRequest(
        @Schema(description = "시간표 강의 id", example = "1", requiredMode = REQUIRED)
        @NotNull(message = "강의 id를 입력해주세요.")
        Integer id,

        @Schema(description = "강의 id", example = "1", requiredMode = NOT_REQUIRED)
        Integer lectureId,

        @Schema(description = "강의 이름", example = "운영체제", requiredMode = NOT_REQUIRED)
        @Size(max = 100, message = "강의 이름의 최대 글자는 100글자입니다.")
        String classTitle,

        @Valid
        @Schema(description = "강의 정보", requiredMode = NOT_REQUIRED)
        List<ClassInfo> classInfos,

        @Schema(description = "강의 교수", example = "이돈우", requiredMode = NOT_REQUIRED)
        @Size(max = 30, message = "교수 명의 최대 글자는 30글자입니다.")
        String professor,

        @Schema(description = "학점", example = "3", requiredMode = REQUIRED)
        @Size(max = 2, message = "학점은 두 글자 이상일 수 없습니다. (0-9)")
        String grades,

        @Schema(description = "memo", example = "메모메모", requiredMode = NOT_REQUIRED)
        @Size(max = 200, message = "메모는 200자 이하로 입력해주세요.")
        String memo
    ) {
        @JsonNaming(value = SnakeCaseStrategy.class)
        public record ClassInfo(
            @Schema(description = "강의 시간", example = "null", requiredMode = NOT_REQUIRED)
            List<Integer> classTime,

            @Schema(description = "강의 장소", example = "도서관", requiredMode = NOT_REQUIRED)
            @Size(max = 30, message = "강의 장소의 최대 글자는 30글자입니다.")
            String classPlace
        ) {

        }

        /**
         * 강의 시간에 -1 구분자를 넣기 위한 메소드
         * -1을 기준으로 강의 시간을 구별합니다.
         */
        public String getClassTimeToString() {
            if (classInfos == null) {
                return null;
            }

            return classInfos.stream()
                .flatMap(info -> Stream.concat(Stream.of(-1), info.classTime.stream()))
                .skip(1)
                .toList()
                .toString();
        }

        /**
         * 강의 장소에 , 구분자를 넣기 위한 메소드
         * , 를 기준으로 강의 장소를 구별합니다.
         */
        public String getClassPlaceToString() {
            if (classInfos == null) {
                return null;
            }

            return classInfos.stream()
                .map(info -> Objects.toString(info.classPlace, ""))
                .collect(Collectors.joining(", "));
        }
    }
}
