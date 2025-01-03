package in.koreatech.koin.domain.timetableV3.utils;

import static lombok.AccessLevel.PRIVATE;

import java.util.List;
import java.util.stream.Stream;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class ClassTimeUtils {

    private static final Integer DIVIDE_TIME_UNIT = 100;

    // 문자열 강의 시간 리스트로 변환
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
}
