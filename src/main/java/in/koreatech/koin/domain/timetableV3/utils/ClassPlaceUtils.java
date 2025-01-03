package in.koreatech.koin.domain.timetableV3.utils;

import static in.koreatech.koin.domain.timetableV3.dto.request.TimetableRegularLectureUpdateRequest.InnerTimeTableRegularLectureRequest.ClassPlace;
import static lombok.AccessLevel.PRIVATE;

import java.util.List;
import java.util.stream.Stream;

import in.koreatech.koin.domain.timetableV3.dto.request.LectureInfo;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class ClassPlaceUtils {

    private static final String CLASSPLACE_SEPARATOR = ", ";

    public static List<String> parseToStringList(String classPlace) {
        return Stream.of(classPlace.split(","))
            .map(String::strip)
            .filter(place -> !place.isEmpty())
            .toList();
    }

    public static String parseToString(List<ClassPlace> classPlaces) {
        StringBuilder classPlaceSegment = new StringBuilder();
        for (int index = 0; index < classPlaces.size(); index++) {
            if (index > 0) {
                classPlaceSegment.append(", ");
            }
            classPlaceSegment.append(classPlaces.get(index).classPlace());
        }
        return classPlaceSegment.toString();
    }

    public static String joinClassPlaces(List<LectureInfo> lectureInfos) {
        StringBuilder classPlaces = new StringBuilder();

        for (int index = 0; index < lectureInfos.size(); index++) {
            if (index > 0) classPlaces.append(CLASSPLACE_SEPARATOR);
            classPlaces.append(lectureInfos.get(index).place());
        }
        return classPlaces.toString();
    }
}
