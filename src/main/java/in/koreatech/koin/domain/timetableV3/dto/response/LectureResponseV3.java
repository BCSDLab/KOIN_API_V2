package in.koreatech.koin.domain.timetableV3.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetable.model.Lecture;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record LectureResponseV3(

    @Schema(description = "과목 id", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "과목 코드", example = "ARB244", requiredMode = REQUIRED)
    String code,

    @Schema(description = "과목 이름", example = "건축구조의 이해 및 실습", requiredMode = REQUIRED)
    String name,

    @Schema(description = "대상 학년", example = "3", requiredMode = REQUIRED)
    String grades,

    @Schema(description = "분반", example = "01", requiredMode = REQUIRED)
    String lectureClass,

    @Schema(description = "수강 인원", example = "25", requiredMode = NOT_REQUIRED)
    String regularNumber,

    @Schema(description = "학부", example = "디자인ㆍ건축공학부", requiredMode = REQUIRED)
    String department,

    @Schema(description = "대상", example = "디자 1 건축", requiredMode = REQUIRED)
    String target,

    @Schema(description = "강의 교수", example = "이돈우", requiredMode = NOT_REQUIRED)
    String professor,

    @Schema(description = "영어 수업 여무", example = "N", requiredMode = REQUIRED)
    String isEnglish,

    @Schema(description = "설계 학점", example = "0", requiredMode = REQUIRED)
    String designScore,

    @Schema(description = "이러닝 여부", example = "Y", requiredMode = REQUIRED)
    String isElearning,

    @Schema(description = "강의 정보", requiredMode = REQUIRED)
    List<LectureInfo> lectureInfos
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record LectureInfo(
        @Schema(description = "요일", example = "0", requiredMode = REQUIRED)
        Integer week,

        @Schema(description = "시작 시간", example = "112", requiredMode = REQUIRED)
        Integer startTime,

        @Schema(description = "종료 시간", example = "115", requiredMode = REQUIRED)
        Integer endTime
    ) {
        private static final Integer DIVIDE_TIME_UNIT = 100;

        public static List<LectureInfo> from(String classTime) {
            List<Integer> classTimes = parseToIntegerList(classTime);
            List<LectureInfo> response = new ArrayList<>();

            if (!classTimes.isEmpty()) {
                Integer prevTime = null;
                Integer startTime = null;
                Integer endTime = null;

                for (Integer time : classTimes) {
                    if (Objects.isNull(prevTime) || time != prevTime + 1) {
                        addLectureInfo(response, startTime, endTime);
                        startTime = time;
                    }
                    endTime = time;
                    prevTime = time;
                }

                addLectureInfo(response, startTime, endTime);
            }
            return response;
        }

        private static void addLectureInfo(List<LectureInfo> response, Integer startTime, Integer endTime) {
            if (!Objects.isNull(startTime)) {
                response.add(new LectureInfo(
                    calcWeek(startTime),
                    startTime,
                    endTime
                ));
            }
        }

        private static Integer calcWeek(Integer startTime) {
            if (startTime != 0) {
                return startTime / DIVIDE_TIME_UNIT;
            }
            return 0;
        }

        private static List<Integer> parseToIntegerList(String classTime) {
            return Stream.of(classTime.replaceAll("[\\[\\]]", "").split(","))
                .map(String::strip)
                .filter(time -> !time.isEmpty())
                .map(Integer::parseInt)
                .toList();
        }
    }

    public static LectureResponseV3 from(Lecture lecture) {
        return new LectureResponseV3(
            lecture.getId(),
            lecture.getCode(),
            lecture.getName(),
            lecture.getGrades(),
            lecture.getLectureClass(),
            lecture.getRegularNumber(),
            lecture.getDepartment(),
            lecture.getTarget(),
            lecture.getProfessor(),
            lecture.getIsEnglish(),
            lecture.getDesignScore(),
            lecture.getIsElearning(),
            LectureInfo.from(lecture.getClassTime())
        );
    }
}
