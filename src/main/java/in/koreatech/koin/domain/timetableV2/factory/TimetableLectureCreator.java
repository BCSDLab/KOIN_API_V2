package in.koreatech.koin.domain.timetableV2.factory;

import static in.koreatech.koin.domain.timetableV2.dto.request.TimetableLectureCreateRequest.*;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetableV2.dto.request.TimetableLectureCreateRequest;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import in.koreatech.koin.domain.timetableV2.repository.LectureRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.TimetableLectureRepositoryV2;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TimetableLectureCreator {

    private final LectureRepositoryV2 lectureRepositoryV2;
    private final TimetableLectureRepositoryV2 timetableLectureRepositoryV2;

    public void createTimetableLectures(TimetableLectureCreateRequest request, TimetableFrame frame) {
        for (InnerTimeTableLectureRequest lectureRequest : request.timetableLecture()) {
            Lecture lecture = determineLecture(lectureRequest.lectureId());
            TimetableLecture timetableLecture = lectureRequest.toTimetableLecture(frame, lecture);
            timetableLectureRepositoryV2.save(timetableLecture);
        }
    }

    private Lecture determineLecture(Integer lectureId) {
        if (lectureId != null) {
            return lectureRepositoryV2.getLectureById(lectureId);
        }
        return null;
    }
}

