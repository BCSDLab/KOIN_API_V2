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
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepository;
import in.koreatech.koin.domain.timetableV2.repository.TimetableLectureRepository;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimetableService {

    private final LectureRepository lectureRepository;
    private final TimetableLectureRepository timetableLectureRepository;
    private final TimetableFrameRepository timetableFrameRepository;
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
        TimetableFrame timetableFrame = timetableFrameRepository.getMainTimetableByUserIdAndSemesterId(userId,
            semester.getId());

        for (TimetableCreateRequest.InnerTimetableRequest timeTable : request.timetable()) {
            Lecture lecture = lectureRepository.getBySemesterAndNameAndLectureClass(request.semester(),
                timeTable.classTitle(), timeTable.lectureClass());
            TimetableLecture timetableLecture = TimetableLecture.builder()
                .classPlace(timeTable.classPlace())
                .grades("0")
                .memo(timeTable.memo())
                .lecture(lecture)
                .timetableFrame(timetableFrame)
                .build();

            timetableLectures.add(timetableLectureRepository.save(timetableLecture));
        }

        return getTimetableResponse(userId, timetableFrame, timetableLectures);
    }

    @Transactional
    public TimetableResponse updateTimetables(Integer userId, TimetableUpdateRequest request) {
        Semester semester = semesterRepository.getBySemester(request.semester());
        TimetableFrame timetableFrame = timetableFrameRepository.getMainTimetableByUserIdAndSemesterId(userId,
            semester.getId());
        for (TimetableUpdateRequest.InnerTimetableRequest timetableRequest : request.timetable()) {
            TimetableLecture timetableLecture = timetableLectureRepository.getById(timetableRequest.id());
            timetableLecture.update(timetableRequest);
            timetableLectureRepository.save(timetableLecture);
        }
        return getTimetableResponse(userId, timetableFrame);
    }

    public TimetableResponse getTimetables(Integer userId, String semesterRequest) {
        Semester semester = semesterRepository.getBySemester(semesterRequest);
        TimetableFrame timetableFrame = timetableFrameRepository.getMainTimetableByUserIdAndSemesterId(userId,
            semester.getId());
        return getTimetableResponse(userId, timetableFrame);
    }

    @Transactional
    public void deleteTimetableLecture(Integer userId, Integer timetableLectureId) {
        TimetableLecture timetableLecture = timetableLectureRepository.getById(timetableLectureId);
        TimetableFrame frame = timetableFrameRepository.getById(timetableLecture.getTimetableFrame().getId());
        if (!Objects.equals(frame.getUser().getId(), userId)) {
            throw AuthorizationException.withDetail("userId: " + userId);
        }

        timetableLectureRepository.deleteById(timetableLectureId);
    }

    private TimetableResponse getTimetableResponse(Integer userId, TimetableFrame timetableFrame) {
        int grades = 0;
        int totalGrades = 0;

        List<TimetableLecture> timetableLectures = timetableLectureRepository.findAllByTimetableFrameId(
            timetableFrame.getId());
        grades = timetableLectures.stream()
            .mapToInt(lecture -> Integer.parseInt(lecture.getLecture().getGrades()))
            .sum();

        for (TimetableFrame timetableFrames : timetableFrameRepository.findByUserIdAndIsMainTrue(userId)) {
            totalGrades += timetableLectureRepository.findAllByTimetableFrameId(timetableFrames.getId()).stream()
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

        if (timetableFrame.getIsMain()) {
            grades = timetableLectures.stream()
                .filter(lecture -> lecture.getLecture() != null)
                .mapToInt(lecture -> Integer.parseInt(lecture.getLecture().getGrades()))
                .sum();
        }
        for (TimetableFrame timetableFrames : timetableFrameRepository.findByUserIdAndIsMainTrue(userId)) {
            totalGrades += timetableLectureRepository.findAllByTimetableFrameId(timetableFrames.getId()).stream()
                .filter(lecture -> lecture.getLecture() != null)
                .mapToInt(lecture -> Integer.parseInt(lecture.getLecture().getGrades()))
                .sum();
        }

        return TimetableResponse.of(timetableLectures, timetableFrame, grades, totalGrades);
    }
}
