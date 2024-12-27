package in.koreatech.koin.domain.timetableV2.dto.request;

import static in.koreatech.koin.domain.timetableV2.exception.TimetableLectureClassTimeNullException.DEFAULT_MESSAGE;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.graduation.model.CourseType;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetableV2.exception.TimetableLectureClassTimeNullException;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record TimetableLectureCreateRequest(
    @Schema(description = "시간표 프레임 id", example = "1213", requiredMode = REQUIRED)
    Integer timetableFrameId,

    @Valid
    @Schema(description = "강의 시간표 정보", requiredMode = REQUIRED)
    @NotNull(message = "시간표 정보를 입력해주세요.")
    List<InnerTimeTableLectureRequest> timetableLecture
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerTimeTableLectureRequest(
        @Schema(description = "강의 이름", example = "null", requiredMode = NOT_REQUIRED)
        @Size(max = 100, message = "강의 이름의 최대 글자는 100글자입니다.")
        String classTitle,

        @Valid
        @Schema(description = "강의 정보", requiredMode = NOT_REQUIRED)
        List<ClassInfo> classInfos,

        @Schema(description = "교수명", example = "null", requiredMode = NOT_REQUIRED)
        @Size(max = 30, message = "교수 명의 최대 글자는 30글자입니다.")
        String professor,

        @Schema(description = "학점", example = "0", requiredMode = REQUIRED)
        @Size(max = 2, message = "학점은 두 글자 이상일 수 없습니다. (0~9)")
        String grades,

        @Schema(description = "메모", example = "메모메모", requiredMode = NOT_REQUIRED)
        @Size(max = 200, message = "메모는 200자 이하로 입력해주세요.")
        String memo,

        @Schema(description = "강의 고유 번호", example = "14", requiredMode = NOT_REQUIRED)
        Integer lectureId
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

        public InnerTimeTableLectureRequest {
            if (lectureId == null) {
                for (ClassInfo classInfo : classInfos) {
                    if (Objects.isNull(classInfo.classTime)) {
                        throw new TimetableLectureClassTimeNullException(DEFAULT_MESSAGE);
                    }
                }
            }

            if (grades == null) {
                grades = "0";
            }
        }

        public TimetableLecture toTimetableLecture(TimetableFrame timetableFrame) {
            return new TimetableLecture(
                classTitle,
                getClassTimeToString(),
                getClassPlaceToString(),
                professor,
                grades,
                memo,
                false,
                null,
                timetableFrame,
                null
            );
        }

        public TimetableLecture toTimetableLecture(TimetableFrame timetableFrame, Lecture lecture, CourseType courseType) {
            return new TimetableLecture(
                classTitle,
                getClassTimeToString(),
                getClassPlaceToString(),
                professor,
                grades,
                memo,
                false,
                lecture,
                timetableFrame,
                courseType
            );
        }

        private String getClassTimeToString() {
            if (classInfos != null) {
                List<Integer> classTimes = new ArrayList<>();
                for (int i = 0; i < classInfos.size(); i++) {
                    if (i > 0)
                        classTimes.add(-1);
                    classTimes.addAll(classInfos.get(i).classTime);
                }
                return classTimes.toString();
            }
            return null;
        }

        private String getClassPlaceToString() {
            if (classInfos != null) {
                StringBuilder classPlaceSegment = new StringBuilder();
                for (int i = 0; i < classInfos.size(); i++) {
                    if (i > 0)
                        classPlaceSegment.append(", ");
                    if (Objects.equals(classInfos.get(i).classPlace, null)) {
                        classPlaceSegment.append("");
                    } else
                        classPlaceSegment.append(classInfos.get(i).classPlace);
                }
                return classPlaceSegment.toString();
            }
            return null;
        }
    }
}
