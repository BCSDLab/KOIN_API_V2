package in.koreatech.koin.domain.timetableV2.dto.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record TimetableLectureResponse(
    @Schema(description = "시간표 프레임 id", example = "1")
    Integer timetableFrameId,

    @Schema(description = "시간표 상세정보")
    List<InnerTimetableLectureResponse> timetable,

    @Schema(description = "해당 학기 학점", example = "21")
    Integer grades,

    @Schema(description = "전체 학기 학점", example = "121")
    Integer totalGrades
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerTimetableLectureResponse(
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
        List<ClassInfo> classInfos,

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
        String department
    ) {
        @JsonNaming(value = SnakeCaseStrategy.class)
        public record ClassInfo(
            @Schema(description = "강의 시간", example = "null", requiredMode = NOT_REQUIRED)
            List<Integer> classTime,

            @Schema(description = "강의 장소", example = "도서관", requiredMode = NOT_REQUIRED)
            String classPlace
        ) {
            public static List<ClassInfo> of(String classTime, String classPlace) {
                if (classPlace == null) {
                    return parseClassInfosWithoutPlace(classTime);
                }
                return parseClassInfosWithPlace(classTime, classPlace);
            }

            private static List<ClassInfo> parseClassInfosWithoutPlace(String classTime) {
                List<ClassInfo> classInfos = new ArrayList<>();
                List<Integer> currentTimes = new ArrayList<>();
                String[] timeSegments = parseTimeSegments(classTime);

                for (String segment : timeSegments) {
                    int time = Integer.parseInt(segment);
                    if (isNonConsecutive(currentTimes, time)) {
                        addClassInfo(classInfos, currentTimes, null);
                    }
                    currentTimes.add(time);
                }

                addClassInfo(classInfos, currentTimes, null);
                return classInfos;
            }

            private static List<ClassInfo> parseClassInfosWithPlace(String classTime, String classPlace) {
                List<ClassInfo> classInfos = new ArrayList<>();
                List<Integer> currentTimes = new ArrayList<>();
                String[] timeSegments = parseTimeSegments(classTime);
                String[] placeSegments = classPlace.split(",\\s*");
                int placeIndex = 0;

                for (String segment : timeSegments) {
                    int time = Integer.parseInt(segment);
                    if (time == -1) {
                        addClassInfo(classInfos, currentTimes, getPlace(placeSegments, placeIndex++));
                    } else {
                        currentTimes.add(time);
                    }
                }

                addClassInfo(classInfos, currentTimes, getPlace(placeSegments, placeIndex));
                return classInfos;
            }

            private static String[] parseTimeSegments(String classTime) {
                return classTime.substring(1, classTime.length() - 1).trim().split(",\\s*");
            }

            private static boolean isNonConsecutive(List<Integer> times, int time) {
                return !times.isEmpty() && times.get(times.size() - 1) + 1 != time;
            }

            private static void addClassInfo(List<ClassInfo> classInfos, List<Integer> times, String place) {
                if (!times.isEmpty()) {
                    classInfos.add(new ClassInfo(new ArrayList<>(times), place));
                    times.clear();
                }
            }

            private static String getPlace(String[] places, int index) {
                return (index < places.length) ? places[index] : "";
            }
        }

        public static List<InnerTimetableLectureResponse> from(List<TimetableLecture> timetableLectures) {
            List<InnerTimetableLectureResponse> timetableLectureList = new ArrayList<>();

            for (TimetableLecture timetableLecture : timetableLectures) {
                Lecture lecture = timetableLecture.getLecture();
                InnerTimetableLectureResponse response;

                if (lecture == null) {
                    response = new InnerTimetableLectureResponse(
                        timetableLecture.getId(),
                        null,
                        null,
                        null,
                        null,
                        ClassInfo.of(timetableLecture.getClassTime(), timetableLecture.getClassPlace()),
                        timetableLecture.getMemo(),
                        timetableLecture.getGrades(),
                        timetableLecture.getClassTitle(),
                        null,
                        null,
                        timetableLecture.getProfessor(),
                        null
                    );
                } else {
                    response = new InnerTimetableLectureResponse(
                        timetableLecture.getId(),
                        lecture.getId(),
                        lecture.getRegularNumber(),
                        lecture.getCode(),
                        lecture.getDesignScore(),
                        ClassInfo.of(getClassTime(timetableLecture, lecture), timetableLecture.getClassPlace()),
                        timetableLecture.getMemo(),
                        getGrades(timetableLecture, lecture),
                        getClassTitle(timetableLecture, lecture),
                        lecture.getLectureClass(),
                        lecture.getTarget(),
                        getProfessor(timetableLecture, lecture),
                        lecture.getDepartment()
                    );
                }
                timetableLectureList.add(response);
            }
            return timetableLectureList;
        }

        private static String getProfessor(TimetableLecture timetableLecture, Lecture lecture) {
            if (timetableLecture.getProfessor() == null) {
                return lecture.getProfessor();
            }
            return timetableLecture.getProfessor();
        }

        private static String getClassTitle(TimetableLecture timetableLecture, Lecture lecture) {
            if (timetableLecture.getClassTitle() == null) {
                return lecture.getName();
            }
            return timetableLecture.getClassTitle();
        }

        private static String getGrades(TimetableLecture timetableLecture, Lecture lecture) {
            if (Objects.equals(timetableLecture.getGrades(), GRADE_ZERO)) {
                return lecture.getGrades();
            }
            return timetableLecture.getGrades();
        }

        private static String getClassTime(TimetableLecture timetableLecture, Lecture lecture) {
            if (timetableLecture.getClassTime() == null) {
                return lecture.getClassTime();
            }
            return timetableLecture.getClassTime();
        }
    }

    private static final String GRADE_ZERO = "0";

    public static TimetableLectureResponse of(TimetableFrame timetableFrame, Integer grades, Integer totalGrades) {
        return new TimetableLectureResponse(
            timetableFrame.getId(),
            InnerTimetableLectureResponse.from(timetableFrame.getTimetableLectures()),
            grades,
            totalGrades
        );
    }
}
