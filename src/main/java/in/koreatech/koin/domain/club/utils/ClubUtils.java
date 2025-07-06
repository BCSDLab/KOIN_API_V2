package in.koreatech.koin.domain.club.utils;

import java.util.List;
import java.util.stream.Collectors;

public class ClubUtils {
    public static String convertImageUrlsToString(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return null;
        }
        return String.format("[%s]", imageUrls.stream()
            .map(url -> "\"" + url + "\"")
            .collect(Collectors.joining(",")));
    }
}
