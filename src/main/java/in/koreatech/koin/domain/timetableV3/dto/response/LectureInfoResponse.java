package in.koreatech.koin.domain.timetableV3.dto.response;

import static in.koreatech.koin.domain.timetableV3.utils.ClassPlaceUtils.parseToStringList;
import static in.koreatech.koin.domain.timetableV3.utils.ClassTimeUtils.calcWeek;
import static in.koreatech.koin.domain.timetableV3.utils.ClassTimeUtils.parseToIntegerList;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record LectureInfoResponse(
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

    public static List<LectureInfoResponse> getRegularLectureInfo(String classTime, String classPlace) {
        List<Integer> classTimes = parseToIntegerList(classTime);
        List<LectureInfoResponse> response = new ArrayList<>();

        // 온라인 강의인 경우
        if (classTimes.isEmpty()) {
            return response;
        }

        List<String> classPlaces = getClassPlaces(classPlace, classTimes);

        Integer prevTime = null;
        Integer startTime = null;
        Integer endTime = null;
        int index = 0;

        for (Integer time : classTimes) {
            if (Objects.isNull(prevTime) || time != prevTime + 1) {
                if (!Objects.isNull(startTime)) {
                    addLectureInfo(response, startTime, endTime, classPlaces.get(index++));
                }
                startTime = time;
            }

            endTime = time;
            prevTime = time;
        }

        if (!Objects.isNull(startTime)) {
            addLectureInfo(response, startTime, endTime, classPlaces.get(index));
        }
        return response;
    }

    // 정규 강의 장소 수정이 없을 경우 빈 장소를 주기 위한 리스트 반환
    private static List<String> getClassPlaces(String classPlace, List<Integer> classTimes) {
        if (Objects.isNull(classPlace) || classPlace.isBlank()) {
            return Collections.nCopies(classTimes.size(), EMPTY_PLACE);
        }
        return parseToStringList(classPlace);
    }

    private static void addLectureInfo(
        List<LectureInfoResponse> response, Integer startTime, Integer endTime, String classPlace
    ) {
        response.add(new LectureInfoResponse(calcWeek(startTime), startTime, endTime, classPlace));
    }

    public static List<LectureInfoResponse> getCustomLectureInfo(String classTime, String classPlace) {
        List<Integer> classTimes = parseToIntegerList(classTime);
        List<LectureInfoResponse> response = new ArrayList<>();

        List<String> classPlaces = getClassPlaces(classPlace, classTimes);

        Integer prevTime = null;
        Integer startTime = null;
        Integer endTime = null;
        int placesIndex = 0;

        for (int time : classTimes) {
            if (Objects.isNull(prevTime)) {
                startTime = time;
            }

            if (!Objects.isNull(prevTime) && prevTime == -1) {
                startTime = time;
                prevTime = null;
                endTime = null;
            }

            if (time == -1) {
                addLectureInfo(response, startTime, endTime, classPlaces.get(placesIndex++));
            } else if (!Objects.isNull(prevTime) && prevTime + 1 != time) {
                addLectureInfo(response, startTime, endTime, classPlaces.get(placesIndex));
                startTime = time;
            }

            endTime = time;
            prevTime = time;
        }

        if (!Objects.isNull(startTime) && prevTime != -1) {
            addLectureInfo(response, startTime, endTime, classPlaces.get(placesIndex));
        }

        return response;
    }
}
