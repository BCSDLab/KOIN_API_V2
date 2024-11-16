package in.koreatech.koin.domain.timetableV2.factory;

import static in.koreatech.koin.domain.timetableV2.dto.request.TimetableLectureUpdateRequest.InnerTimetableLectureRequest;

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
                timetableRequest.classTime().toString(),
                timetableRequest.classPlace(),
                timetableRequest.professor(),
                timetableRequest.grades(),
                timetableRequest.memo()
            );
        }
    }
}
