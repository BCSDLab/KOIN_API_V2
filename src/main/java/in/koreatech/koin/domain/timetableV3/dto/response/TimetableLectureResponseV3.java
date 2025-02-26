package in.koreatech.koin.domain.timetableV3.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static com.google.common.base.MoreObjects.firstNonNull;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record TimetableLectureResponseV3(
    @Schema(description = "시간표 프레임 id", example = "1")
    Integer timetableFrameId,

    @Schema(description = "시간표 프레임 강의 정보")
    List<InnerTimetableLectureResponseV3> timetable,

    @Schema(description = "해당 학기 학점", example = "24")
    Integer grades,

    @Schema(description = "전체 학기 학점", example = "131")
    Integer totalGrades
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerTimetableLectureResponseV3(
        @Schema(description = "시간표 id", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "강의 id", example = "1", requiredMode = NOT_REQUIRED)
        Integer lectureId,

        @Schema(description = "수강 정원", example = "38", requiredMode = NOT_REQUIRED)
        String regularNumber,

        @Schema(description = "과목 코드", example = "ARB244", requiredMode = NOT_REQUIRED)
        String code,

        @Schema(description = "설계 학점", example = "0", requiredMode = NOT_REQUIRED)
        String designScore,

        @Schema(description = "강의 정보", requiredMode = NOT_REQUIRED)
        List<LectureInfoResponse> lectureInfos,

        @Schema(description = "메모", example = "null", requiredMode = NOT_REQUIRED)
        String memo,

        @Schema(description = "학점", example = "3", requiredMode = REQUIRED)
        String grades,

        @Schema(description = "강의(커스텀) 이름", example = "한국사", requiredMode = REQUIRED)
        String classTitle,

        @Schema(description = "분반", example = "01", requiredMode = NOT_REQUIRED)
        String lectureClass,

        @Schema(description = "대상", example = "디자 1 건축", requiredMode = NOT_REQUIRED)
        String target,

        @Schema(description = "강의 교수", example = "이돈우", requiredMode = NOT_REQUIRED)
        String professor,

        @Schema(description = "학부", example = "디자인ㆍ건축공학부", requiredMode = NOT_REQUIRED)
        String department,

        @Schema(description = "이수구분", example = "전공필수", requiredMode = NOT_REQUIRED)
        String courseType,

        @Schema(description = "교양영역", example = "", requiredMode = NOT_REQUIRED)
        String generalEducationArea
    ) {
        public static List<InnerTimetableLectureResponseV3> from(List<TimetableLecture> timetableLectures) {
            List<InnerTimetableLectureResponseV3> InnerTimetableLectureResponses = new ArrayList<>();

            for (TimetableLecture timetableLecture : timetableLectures) {
                Lecture lecture = timetableLecture.getLecture();
                InnerTimetableLectureResponseV3 responseV3;

                if (lecture == null) {
                    responseV3 = new InnerTimetableLectureResponseV3(
                        timetableLecture.getId(),
                        null,
                        null,
                        null,
                        null,
                        LectureInfoResponse.getCustomLectureInfo(timetableLecture.getClassTime(),
                            timetableLecture.getClassPlace()),
                        timetableLecture.getMemo(),
                        timetableLecture.getGrades(),
                        timetableLecture.getClassTitle(),
                        null,
                        null,
                        timetableLecture.getProfessor(),
                        null,
                        getCourseType(timetableLecture),
                        getGeneralEducationArea(timetableLecture)
                    );
                } else {
                    responseV3 = new InnerTimetableLectureResponseV3(
                        timetableLecture.getId(),
                        lecture.getId(),
                        lecture.getRegularNumber(),
                        lecture.getCode(),
                        lecture.getDesignScore(),
                        LectureInfoResponse.getRegularLectureInfo(
                            firstNonNull(timetableLecture.getClassTime(), lecture.getClassTime()),
                            timetableLecture.getClassPlace()),
                        timetableLecture.getMemo(),
                        lecture.getGrades(),
                        lecture.getName(),
                        lecture.getLectureClass(),
                        lecture.getTarget(),
                        firstNonNull(timetableLecture.getProfessor(),lecture.getProfessor()),
                        lecture.getDepartment(),
                        getCourseType(timetableLecture),
                        getGeneralEducationArea(timetableLecture)
                    );
                }
                InnerTimetableLectureResponses.add(responseV3);
            }
            return InnerTimetableLectureResponses;
        }

        private static String getCourseType(TimetableLecture timetableLecture) {
            if (Objects.isNull(timetableLecture.getCourseType())) {
                return "이수구분선택";
            }
            return timetableLecture.getCourseType().getName();
        }

        private static String getGeneralEducationArea(TimetableLecture timetableLecture) {
            if (timetableLecture.getGeneralEducationArea() == null) {
                return "";
            }
            return timetableLecture.getGeneralEducationArea().getName();
        }
    }

    public static TimetableLectureResponseV3 of(TimetableFrame frame, int grades, int totalGrades) {
        return new TimetableLectureResponseV3(
            frame.getId(),
            InnerTimetableLectureResponseV3.from(frame.getTimetableLectures()),
            grades,
            totalGrades
        );
    }
}
