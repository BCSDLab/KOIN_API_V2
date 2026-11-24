package in.koreatech.koin.domain.timetableV3.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.*;
import static com.google.common.base.MoreObjects.firstNonNull;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.language.service.OfficialNameResolver;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record TakeAllTimetableLectureResponse(
    @Schema(description = "시간표 프레임 강의 정보")
    List<TakeAllTimetableLectureResponse.InnerTimetableLectureResponseV3> timetable
) {
    public TakeAllTimetableLectureResponse withLanguage(SupportedLanguage language) {
        return new TakeAllTimetableLectureResponse(timetable.stream().map(lecture -> lecture.withLanguage(language)).toList());
    }

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
        String generalEducationArea,

        @Schema(description = "표시용 공식 강의명. 미등록 또는 직접 작성한 강의는 class_title 원문")
        String displayName
    ) {
        public InnerTimetableLectureResponseV3 withLanguage(SupportedLanguage language) {
            return new InnerTimetableLectureResponseV3(id, lectureId, regularNumber, code, designScore,
                lectureInfos, memo, grades, classTitle, lectureClass, target, professor, department,
                courseType, generalEducationArea,
                lectureId == null ? classTitle : OfficialNameResolver.lectureName(classTitle, language));
        }

        public static List<TakeAllTimetableLectureResponse.InnerTimetableLectureResponseV3> from(
            List<TimetableLecture> timetableLectures) {
            List<TakeAllTimetableLectureResponse.InnerTimetableLectureResponseV3> InnerTimetableLectureResponses = new ArrayList<>();

            for (TimetableLecture timetableLecture : timetableLectures) {
                Lecture lecture = timetableLecture.getLecture();
                TakeAllTimetableLectureResponse.InnerTimetableLectureResponseV3 responseV3;

                if (lecture == null) {
                    responseV3 = new TakeAllTimetableLectureResponse.InnerTimetableLectureResponseV3(
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
                        getGeneralEducationArea(timetableLecture),
                        timetableLecture.getClassTitle()
                    );
                } else {
                    responseV3 = new TakeAllTimetableLectureResponse.InnerTimetableLectureResponseV3(
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
                        timetableLecture.getProfessor() != null
                            ? timetableLecture.getProfessor() : lecture.getProfessor(),
                        lecture.getDepartment(),
                        getCourseType(timetableLecture),
                        getGeneralEducationArea(timetableLecture),
                        lecture.getName()
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

        public static TakeAllTimetableLectureResponse of(TimetableFrame frame) {
            return new TakeAllTimetableLectureResponse(
                TakeAllTimetableLectureResponse.InnerTimetableLectureResponseV3.from(frame.getTimetableLectures())
            );
        }
    }
}
