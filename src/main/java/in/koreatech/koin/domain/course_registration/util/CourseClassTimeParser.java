package in.koreatech.koin.domain.course_registration.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CourseClassTimeParser {

    /**
     * classTime 문자열 "[1, 2, 101, 102]" 에서 정수 배열 추출
     */
    public static List<Integer> extractRawClassTime(String classTime) {
        if (classTime == null || classTime.isEmpty()) {
            return Collections.emptyList();
        }

        String cleaned = classTime.replaceAll("[\\[\\]\\s]", "");
        if (cleaned.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            return Arrays.stream(cleaned.split(","))
                .map(Integer::parseInt)
                .sorted()
                .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            return Collections.emptyList();
        }
    }

    /**
     * classTime 문자열을 파싱하여 "월01A~03B,화01A~03B" 형식으로 변환
     * [1, 2, 101, 102] -> "월01A~01B,화01A-01B"
     */
    public static String parseClassTime(List<Integer> timeSlots) {
        if (timeSlots == null || timeSlots.isEmpty()) {
            return "";
        }

        Map<String, List<Integer>> dayGroups = new LinkedHashMap<>();
        dayGroups.put("월", new ArrayList<>());
        dayGroups.put("화", new ArrayList<>());
        dayGroups.put("수", new ArrayList<>());
        dayGroups.put("목", new ArrayList<>());
        dayGroups.put("금", new ArrayList<>());

        for (Integer slot : timeSlots) {
            int day = slot / 100;
            int period = slot % 100;

            String dayName = switch (day) {
                case 0 -> "월";
                case 1 -> "화";
                case 2 -> "수";
                case 3 -> "목";
                case 4 -> "금";
                default -> null;
            };

            if (dayName != null && period <= 5) {
                dayGroups.get(dayName).add(period);
            }
        }

        List<String> result = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> entry : dayGroups.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }

            String dayName = entry.getKey();
            List<Integer> periods = entry.getValue();
            String timeRange = formatTimeRange(periods);
            result.add(dayName + timeRange);
        }

        return String.join(",", result);
    }

    private static String formatTimeRange(List<Integer> periods) {
        if (periods.isEmpty()) {
            return "";
        }

        Collections.sort(periods);
        int start = periods.get(0);
        int end = periods.get(periods.size() - 1);

        if (start == end) {
            return convertToTimeCode(start);
        }

        return convertToTimeCode(start) + "~" + convertToTimeCode(end);
    }

    private static String convertToTimeCode(int period) {
        int hour = (period / 2) + 1;
        String ab = (period % 2 == 0) ? "A" : "B";
        return String.format("%02d%s", hour, ab);
    }
}
