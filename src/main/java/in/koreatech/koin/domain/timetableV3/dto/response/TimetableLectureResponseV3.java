package in.koreatech.koin.domain.timetableV3.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static in.koreatech.koin.domain.timetableV3.utils.ClassPlaceUtils.parseToStringList;
import static in.koreatech.koin.domain.timetableV3.utils.ClassTimeUtils.calcWeek;
import static in.koreatech.koin.domain.timetableV3.utils.ClassTimeUtils.parseToIntegerList;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import in.koreatech.koin.domain.timetableV3.model.TimetableLectureInformation;
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
        List<LectureInfo> lectureInfos,

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
        public record LectureInfo(
            @Schema(description = "요일 id", example = "0", requiredMode = REQUIRED)
            Integer week,

            @Schema(description = "시작 시간", example = "112", requiredMode = REQUIRED)
            Integer startTime,

            @Schema(description = "종료 시간", example = "115", requiredMode = REQUIRED)
            Integer endTime,

            @Schema(description = "장소", example = "2공학관314", requiredMode = NOT_REQUIRED)
            String place
        ) {
            private static final String EMPTY_PLACE = "";

            public static List<LectureInfo> getCustomLectureInfo(
                List<TimetableLectureInformation> timetableLectureInformations
            ) {
                return timetableLectureInformations.stream()
                    .map(timetableCustomLectureInformation -> new LectureInfo(
                        calcWeek(timetableCustomLectureInformation.getStartTime()),
                        timetableCustomLectureInformation.getStartTime(),
                        timetableCustomLectureInformation.getEndTime(),
                        getResponsePlace(timetableCustomLectureInformation.getPlace())
                    ))
                    .collect(Collectors.toList());
            }

            public static List<LectureInfo> getRegularLectureInfo(String classTime, String classPlace) {
                List<Integer> classTimes = parseToIntegerList(classTime);
                List<String> classPlaces = parseToStringList(classPlace);
                List<LectureInfo> response = new ArrayList<>();
                int index = 0;

                if (!classTimes.isEmpty()) {
                    Integer prevTime = null;
                    Integer startTime = null;
                    Integer endTime = null;

                    for (Integer time : classTimes) {
                        if (Objects.isNull(prevTime) || time != prevTime + 1) {
                            addLectureInfo(response, startTime, endTime, classPlaces.get(index));
                            startTime = time;
                            index++;
                        }
                        endTime = time;
                        prevTime = time;
                    }

                    addLectureInfo(response, startTime, endTime, classPlaces.get(index));
                }
                return response;
            }

            private static void addLectureInfo(
                List<LectureInfo> response, Integer startTime, Integer endTime, String classPlace
            ) {
                if (!Objects.isNull(startTime)) {
                    response.add(new LectureInfo(calcWeek(startTime), startTime, endTime, classPlace));
                }
            }

            private static String getResponsePlace(String place) {
                if (Objects.isNull(place)) {
                    return "";
                }
                return place;
            }
        }

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
                        LectureInfo.getCustomLectureInfo(timetableLecture.getTimetableLectureInformations()),
                        timetableLecture.getMemo(),
                        timetableLecture.getGrades(),
                        timetableLecture.getClassTitle(),
                        null,
                        null,
                        timetableLecture.getProfessor(),
                        null
                    );
                } else {
                    responseV3 = new InnerTimetableLectureResponseV3(
                        timetableLecture.getId(),
                        lecture.getId(),
                        lecture.getRegularNumber(),
                        lecture.getCode(),
                        lecture.getDesignScore(),
                        LectureInfo.getRegularLectureInfo(lecture.getClassTime(), timetableLecture.getClassPlace()),
                        timetableLecture.getMemo(),
                        lecture.getGrades(),
                        timetableLecture.getClassTitle() == null ? lecture.getName() : timetableLecture.getClassTitle(),
                        lecture.getLectureClass(),
                        lecture.getTarget(),
                        lecture.getProfessor(),
                        lecture.getDepartment()
                    );
                }
                InnerTimetableLectureResponses.add(responseV3);
            }
            return InnerTimetableLectureResponses;
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
