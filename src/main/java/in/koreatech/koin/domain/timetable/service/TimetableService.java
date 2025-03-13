package in.koreatech.koin.domain.timetable.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.auth.exception.AuthorizationException;
import in.koreatech.koin._common.exception.custom.RequestTooFastException;
import in.koreatech.koin.domain.timetable.dto.LectureResponse;
import in.koreatech.koin.domain.timetable.dto.TimetableCreateRequest;
import in.koreatech.koin.domain.timetable.dto.TimetableResponse;
import in.koreatech.koin.domain.timetable.dto.TimetableUpdateRequest;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import in.koreatech.koin.domain.timetableV2.repository.LectureRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.SemesterRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.TimetableLectureRepositoryV2;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimetableService {

    private final LectureRepositoryV2 lectureRepositoryV2;
    private final TimetableLectureRepositoryV2 timetableLectureRepositoryV2;
    private final TimetableFrameRepositoryV2 timetableFrameRepositoryV2;
    private final SemesterRepositoryV2 semesterRepositoryV2;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    public List<LectureResponse> getLecturesBySemester(String semester) {
        semesterRepositoryV2.getBySemester(semester);
        List<Lecture> lectures = lectureRepositoryV2.findBySemester(semester);
        return lectures.stream()
            .map(LectureResponse::from)
            .toList();
    }

    @Transactional
    public TimetableResponse createTimetables(Integer userId, TimetableCreateRequest request) {
        Semester semester = semesterRepositoryV2.getBySemester(request.semester());
        List<TimetableLecture> timetableLectures = new ArrayList<>();
        TimetableFrame timetableFrame = timetableFrameRepositoryV2.getMainTimetableFrame(userId,
            semester.getId());

        for (TimetableCreateRequest.InnerTimetableRequest timeTable : request.timetable()) {
            Lecture lecture = lectureRepositoryV2.getBySemesterAndCodeAndLectureClass(request.semester(),
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
        Semester semester = semesterRepositoryV2.getBySemester(request.semester());
        TimetableFrame timetableFrame = timetableFrameRepositoryV2.getMainTimetableFrame(userId,
            semester.getId());
        for (TimetableUpdateRequest.InnerTimetableRequest timetableRequest : request.timetable()) {
            TimetableLecture timetableLecture = timetableLectureRepositoryV2.getById(timetableRequest.id());
            timetableLecture.update(
                timetableRequest.classTitle(),
                timetableRequest.classTime().toString(),
                timetableRequest.classPlace(),
                timetableRequest.professor(),
                timetableRequest.grades(),
                timetableRequest.memo()
            );
            timetableLectureRepositoryV2.save(timetableLecture);
        }
        return getTimetableResponse(userId, timetableFrame);
    }

    @Transactional
    public TimetableResponse getTimetables(Integer userId, String semesterRequest) {
        Semester semester = semesterRepositoryV2.getBySemester(semesterRequest);
        User user = userRepository.getById(userId);

        Optional<TimetableFrame> timetableFrame = timetableFrameRepositoryV2.findByUserIdAndSemesterIdAndIsMainTrue(
            userId, semester.getId());

        if (timetableFrame.isEmpty()) {
            TimetableFrame newTimetableFrame = TimetableFrame
                .builder()
                .user(user)
                .semester(semester)
                .name("시간표1")
                .isMain(true)
                .isDeleted(false)
                .build();
            timetableFrameRepositoryV2.save(newTimetableFrame);
        }

        TimetableFrame frame = timetableFrameRepositoryV2.getMainTimetableFrame(userId,
            semester.getId());

        return getTimetableResponse(userId, frame);
    }

    @Transactional
    public void deleteTimetableLecture(Integer userId, Integer timetableLectureId) {
        try {
            TimetableLecture timetableLecture = timetableLectureRepositoryV2.getById(timetableLectureId);
            TimetableFrame frame = timetableFrameRepositoryV2.getById(timetableLecture.getTimetableFrame().getId());
            if (!Objects.equals(frame.getUser().getId(), userId)) {
                throw AuthorizationException.withDetail("userId: " + userId);
            }
            timetableLecture.delete();
            entityManager.flush();
        } catch (OptimisticLockException e) {
            throw new RequestTooFastException("요청이 너무 빠릅니다. 다시 시도해주세요.");
        }
    }

    private TimetableResponse getTimetableResponse(Integer userId, TimetableFrame timetableFrame) {
        int grades = 0;
        int totalGrades = 0;

        List<TimetableLecture> timetableLectures = timetableLectureRepositoryV2.findAllByTimetableFrameId(
            timetableFrame.getId());
        grades = timetableLectures.stream()
            .filter(lecture -> lecture.getLecture() != null)
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
}
