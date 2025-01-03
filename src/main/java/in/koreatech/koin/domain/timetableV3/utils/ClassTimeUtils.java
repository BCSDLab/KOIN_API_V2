package in.koreatech.koin.domain.timetableV3.utils;

import static lombok.AccessLevel.PRIVATE;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import in.koreatech.koin.domain.timetableV3.dto.request.LectureInfoRequest;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class ClassTimeUtils {

    private static final Integer DIVIDE_TIME_UNIT = 100;
    private static final Integer CLASSTIME_SEPARATOR = -1;

    // 정규 강의 시간 리스트로 변환
    public static List<Integer> parseToIntegerList(String classTime) {
        return Stream.of(classTime.replaceAll("[\\[\\]]", "").split(","))
            .map(String::strip)
            .filter(time -> !time.isEmpty())
            .map(Integer::parseInt)
            .toList();
    }

    // week 계산
    public static Integer calcWeek(Integer startTime) {
        if (startTime != 0) {
            return startTime / DIVIDE_TIME_UNIT;
        }
        return 0;
    }

    // 커스텀 강의 시간 조인
    public static String joinClassTimes(List<LectureInfoRequest> lectureInfos) {
        List<Integer> classTimes = new ArrayList<>();

        for (int index = 0; index < lectureInfos.size(); index++) {
            if (index > 0) classTimes.add(CLASSTIME_SEPARATOR);

            int startTime = lectureInfos.get(index).startTime();
            int endTime = lectureInfos.get(index).endTime();

            while (startTime <= endTime) {
                classTimes.add(startTime++);
            }
        }
        return classTimes.toString();
    }
}
