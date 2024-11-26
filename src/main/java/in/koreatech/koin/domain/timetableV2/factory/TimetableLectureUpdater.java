package in.koreatech.koin.domain.timetableV2.factory;

import static in.koreatech.koin.domain.timetableV2.dto.request.TimetableLectureUpdateRequest.InnerTimetableLectureRequest;
import static in.koreatech.koin.domain.timetableV2.dto.request.TimetableLectureUpdateRequest.InnerTimetableLectureRequest.ClassInfo;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.timetableV2.dto.request.TimetableLectureUpdateRequest;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import in.koreatech.koin.domain.timetableV2.repository.TimetableLectureRepositoryV2;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TimetableLectureUpdater {

    private final TimetableLectureRepositoryV2 timetableLectureRepositoryV2;

    public void updateTimetablesLectures(TimetableLectureUpdateRequest request) {
        for (InnerTimetableLectureRequest timetableRequest : request.timetableLecture()) {
            TimetableLecture timetableLecture = timetableLectureRepositoryV2.getById(timetableRequest.id());
            timetableLecture.update(
                timetableRequest.classTitle(),
                getClassTimeToString(timetableRequest.classInfos()),
                getClassPlaceToString(timetableRequest.classInfos()),
                timetableRequest.professor(),
                timetableRequest.grades(),
                timetableRequest.memo()
            );
        }
    }

    private String getClassTimeToString(List<ClassInfo> classInfos) {
        if (classInfos != null) {
            List<Integer> classTimes = classInfos.stream()
                .flatMap(c -> concat(c.classTime().stream(), of(-1)))
                .toList();
            return classTimes.toString();
        }
        return null;
    }

    private String getClassPlaceToString(List<ClassInfo> classInfos) {
        if (classInfos != null) {
            return classInfos.stream()
                .map(ClassInfo::classPlace)
                .collect(Collectors.joining(", "));
        }
        return null;
    }
}
