package in.koreatech.koin.domain.timetableV3.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static in.koreatech.koin.domain.timetableV3.utils.ClassTimeUtils.calcWeek;
import static in.koreatech.koin.domain.timetableV3.utils.ClassTimeUtils.parseToIntegerList;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record LectureInfoResponse(
    @Schema(description = "요일", example = "0", requiredMode = REQUIRED)
    Integer day,

    @Schema(description = "시작 시간", example = "112", requiredMode = REQUIRED)
    Integer startTime,

    @Schema(description = "종료 시간", example = "115", requiredMode = REQUIRED)
    Integer endTime,

    @Schema(description = "장소", example = "2공학관314", requiredMode = NOT_REQUIRED)
    String place
) {
    private static final String EMPTY_PLACE = "";

    // 역정규화 된 정규 강의 정보를 정규화 하는 메소드
    public static List<LectureInfoResponse> getRegularLectureInfo(String classTime, String classPlace) {
        List<Integer> classTimes = parseToIntegerList(classTime);
        List<LectureInfoResponse> response = new ArrayList<>();

        // 온라인 강의인 경우
        if (classTimes.isEmpty()) {
            return response;
        }

        List<String> classPlaces = getClassPlaces(classPlace, classTimes);

        int startTime = classTimes.get(0);
        int endTime = startTime;
        int classPlaceIndex = 0;

        for (int index = 1; index < classTimes.size(); index++) {
            if (classTimes.get(index) == endTime + 1) {
                endTime = classTimes.get(index);
            } else {
                if (classPlaceIndex + 1 > classPlaces.size()) {
                    addLectureInfo(response, startTime, endTime, EMPTY_PLACE);
                } else {
                    addLectureInfo(response, startTime, endTime, classPlaces.get(classPlaceIndex++));
                }
                startTime = classTimes.get(index);
                endTime = startTime;
            }
        }

        if (classPlaceIndex + 1 > classPlaces.size()) {
            addLectureInfo(response, startTime, endTime, EMPTY_PLACE);
        } else {
            addLectureInfo(response, startTime, endTime, classPlaces.get(classPlaceIndex));
        }
        return response;
    }

    // 역정규화 된 커스텀 강의 정보를 정규화 하는 메소드
    public static List<LectureInfoResponse> getCustomLectureInfo(String classTime, String classPlace) {
        if (classTime == null || classPlace == null) {
            return Collections.emptyList();
        }

        List<Integer> classTimes = parseToIntegerList(classTime);
        if (classTimes.isEmpty()) {
            return Collections.emptyList();
        }
        List<LectureInfoResponse> response = new ArrayList<>();

        List<String> classPlaces = getClassPlaces(classPlace, classTimes);

        int startTime = classTimes.get(0);
        int endTime = startTime;
        int placesIndex = 0;

        for (int index = 1; index < classTimes.size(); index++) {
            int time = classTimes.get(index);

            if (endTime == -1) {
                startTime = time;
                endTime = startTime;
                continue;
            }

            if (time == endTime + 1) {
                endTime = time;
            } else {
                if (time == -1) {
                    addLectureInfo(response, startTime, endTime, classPlaces.get(placesIndex++));
                } else {
                    addLectureInfo(response, startTime, endTime, classPlaces.get(placesIndex));
                }
                startTime = time;
                endTime = startTime;
            }
        }

        if (endTime != -1) {
            addLectureInfo(response, startTime, endTime, classPlaces.get(placesIndex));
        }

        return response;
    }

    // 강의 장소가 null 혹은 빈 값인 경우 강의 장소를 공백으로 넘기기 위한 메소드
    private static List<String> getClassPlaces(String classPlace, List<Integer> classTimes) {
        if (Objects.isNull(classPlace) || classPlace.isBlank()) {
            return Collections.nCopies(classTimes.size(), EMPTY_PLACE);
        }
        return parseToStringList(classPlace);
    }

    // 문자열 강의 장소 리스트로 변환
    private static List<String> parseToStringList(String classPlace) {
        return Stream.of(classPlace.split(","))
            .map(String::strip)
            .toList();
    }

    private static void addLectureInfo(
        List<LectureInfoResponse> response, Integer startTime, Integer endTime, String classPlace
    ) {
        response.add(new LectureInfoResponse(calcWeek(startTime), startTime, endTime, classPlace));
    }
}
