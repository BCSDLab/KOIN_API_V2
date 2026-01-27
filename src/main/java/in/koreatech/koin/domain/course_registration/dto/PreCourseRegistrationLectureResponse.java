package in.koreatech.koin.domain.course_registration.dto;

import static in.koreatech.koin.domain.course_registration.util.CourseClassTimeParser.extractRawClassTime;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record PreCourseRegistrationLectureResponse (

    @Schema(description = "학과", example = "컴퓨터공학부", requiredMode = REQUIRED)
    String department,

    @Schema(description = "교과목명", example = "1", requiredMode = REQUIRED)
    InnerLectureInfo lectureInfo,

    @Schema(description = "분반", example = "01", requiredMode = REQUIRED)
    String classNumber,

    @Schema(description = "학점", example = "3", requiredMode = REQUIRED)
    String grades,

    @Schema(description = "수업 교시 원본 데이터", example = "[0,1,2,3,4,5,100,101,102,103,104,105]", requiredMode = REQUIRED)
    List<Integer> classTimeRaw
) {

    public static PreCourseRegistrationLectureResponse from(TimetableLecture timetableLecture) {
        Lecture lecture = timetableLecture.getLecture();

        String lectureCode;
        String lectureName;
        String classNumber;
        String grades;
        List<Integer> classTimeRaw;

        // 커스텀 강의
        if (lecture == null) {
            lectureCode = null;
            lectureName = timetableLecture.getClassTitle();
            classNumber = null;
            grades = timetableLecture.getGrades();
            classTimeRaw = extractRawClassTime(timetableLecture.getClassTime());
        } else { // 일반 강의
            lectureCode = lecture.getCode();
            lectureName = lecture.getName();
            classNumber = lecture.getLectureClass();
            grades = lecture.getGrades();
            String classTime = timetableLecture.getClassTime() != null
                ? timetableLecture.getClassTime()
                : lecture.getClassTime();
            classTimeRaw = extractRawClassTime(classTime);
        }

        return new PreCourseRegistrationLectureResponse(
            getDepartment(timetableLecture),
            new InnerLectureInfo(lectureCode, lectureName),
            classNumber,
            grades,
            classTimeRaw
        );
    }

    public static List<PreCourseRegistrationLectureResponse> fromList(List<TimetableLecture> timetableLectures) {
        return timetableLectures.stream()
            .map(PreCourseRegistrationLectureResponse::from)
            .toList();
    }

    private static String getDepartment(TimetableLecture timetableLecture) {
        if (timetableLecture.getLecture() == null ||
            Objects.isNull(timetableLecture.getLecture().getDepartment())) {
            return "-";
        }
        return timetableLecture.getLecture().getDepartment();
    }
}
