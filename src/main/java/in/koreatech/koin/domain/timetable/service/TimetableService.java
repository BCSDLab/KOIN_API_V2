package in.koreatech.koin.domain.timetable.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.timetable.dto.LectureResponse;
import in.koreatech.koin.domain.timetable.dto.TimetableCreateRequest;
import in.koreatech.koin.domain.timetable.dto.TimetableResponse;
import in.koreatech.koin.domain.timetable.dto.TimetableUpdateRequest;
import in.koreatech.koin.domain.timetable.exception.SemesterNotFoundException;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetable.repository.LectureRepository;
import in.koreatech.koin.domain.timetable.repository.SemesterRepository;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.TimetableLectureRepositoryV2;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimetableService {

    private final LectureRepository lectureRepository;
    private final TimetableLectureRepositoryV2 timetableLectureRepositoryV2;
    private final TimetableFrameRepositoryV2 timetableFrameRepositoryV2;
    private final SemesterRepository semesterRepository;

    public List<LectureResponse> getLecturesBySemester(String semester) {
        List<Lecture> lectures = lectureRepository.findBySemester(semester);
        if (lectures.isEmpty()) {
            throw SemesterNotFoundException.withDetail(semester);
        }
        return lectures.stream()
            .map(LectureResponse::from)
            .toList();
    }

    @Transactional
    public TimetableResponse createTimetables(Integer userId, TimetableCreateRequest request) {
        Semester semester = semesterRepository.getBySemester(request.semester());
        List<TimetableLecture> timetableLectures = new ArrayList<>();
        TimetableFrame timetableFrame = timetableFrameRepositoryV2.getMainTimetableByUserIdAndSemesterId(userId,
            semester.getId());

        for (TimetableCreateRequest.InnerTimetableRequest timeTable : request.timetable()) {
            Lecture lecture = lectureRepository.getBySemesterAndCodeAndLectureClass(request.semester(),
                timeTable.code(), timeTable.lectureClass());
            TimetableLecture timetableLecture = TimetableLecture.builder()
                .classPlace(timeTable.classPlace())
                .grades("0")
                .memo(timeTable.memo())
                .lecture(lecture)
                .timetableFrame(timetableFrame)
                .build();

            timetableLectures.add(timetableLectureRepositoryV2.save(timetableLecture));
        }

        return getTimetableResponse(userId, timetableFrame);
    }

    @Transactional
    public TimetableResponse updateTimetables(Integer userId, TimetableUpdateRequest request) {
        Semester semester = semesterRepository.getBySemester(request.semester());
        TimetableFrame timetableFrame = timetableFrameRepositoryV2.getMainTimetableByUserIdAndSemesterId(userId,
            semester.getId());
        for (TimetableUpdateRequest.InnerTimetableRequest timetableRequest : request.timetable()) {
            TimetableLecture timetableLecture = timetableLectureRepositoryV2.getById(timetableRequest.id());
            timetableLecture.update(timetableRequest);
            timetableLectureRepositoryV2.save(timetableLecture);
        }
        return getTimetableResponse(userId, timetableFrame);
    }

    public TimetableResponse getTimetables(Integer userId, String semesterRequest) {
        Semester semester = semesterRepository.getBySemester(semesterRequest);
        TimetableFrame timetableFrame = timetableFrameRepositoryV2.getMainTimetableByUserIdAndSemesterId(userId,
            semester.getId());
        return getTimetableResponse(userId, timetableFrame);
    }

    @Transactional
    public void deleteTimetableLecture(Integer userId, Integer timetableLectureId) {
        TimetableLecture timetableLecture = timetableLectureRepositoryV2.getById(timetableLectureId);
        TimetableFrame frame = timetableFrameRepositoryV2.getById(timetableLecture.getTimetableFrame().getId());
        if (!Objects.equals(frame.getUser().getId(), userId)) {
            throw AuthorizationException.withDetail("userId: " + userId);
        }

        timetableLectureRepositoryV2.deleteById(timetableLectureId);
    }

    private TimetableResponse getTimetableResponse(Integer userId, TimetableFrame timetableFrame) {
        int grades = 0;
        int totalGrades = 0;

        List<TimetableLecture> timetableLectures = timetableLectureRepositoryV2.findAllByTimetableFrameId(
            timetableFrame.getId());
        grades = timetableLectures.stream()
            .mapToInt(lecture -> Integer.parseInt(lecture.getLecture().getGrades()))
            .sum();

        for (TimetableFrame timetableFrames : timetableFrameRepositoryV2.findByUserIdAndIsMainTrue(userId)) {
            totalGrades += timetableLectureRepositoryV2.findAllByTimetableFrameId(timetableFrames.getId()).stream()
                .filter(lecture -> lecture.getLecture() != null)
                .mapToInt(lecture -> Integer.parseInt(lecture.getLecture().getGrades()))
                .sum();
        }

        return TimetableResponse.of(timetableLectures, timetableFrame, grades, totalGrades);
    }

    private TimetableResponse getTimetableResponse(Integer userId, TimetableFrame timetableFrame,
        List<TimetableLecture> timetableLectures) {
        int grades = 0;
        int totalGrades = 0;

        if (timetableFrame.isMain()) {
            grades = timetableLectures.stream()
                .filter(lecture -> lecture.getLecture() != null)
                .mapToInt(lecture -> Integer.parseInt(lecture.getLecture().getGrades()))
                .sum();
        }
        for (TimetableFrame timetableFrames : timetableFrameRepositoryV2.findByUserIdAndIsMainTrue(userId)) {
            totalGrades += timetableLectureRepositoryV2.findAllByTimetableFrameId(timetableFrames.getId()).stream()
                .filter(lecture -> lecture.getLecture() != null)
                .mapToInt(lecture -> Integer.parseInt(lecture.getLecture().getGrades()))
                .sum();
        }

        return TimetableResponse.of(timetableLectures, timetableFrame, grades, totalGrades);
    }
}
