package in.koreatech.koin.domain.timetable.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.timetable.dto.LectureResponse;
import in.koreatech.koin.domain.timetable.dto.TimeTableCreateRequest;
import in.koreatech.koin.domain.timetable.dto.TimetableLectureResponse;
import in.koreatech.koin.domain.timetable.dto.TimetableLectureUpdateRequest;
import in.koreatech.koin.domain.timetable.dto.TimetableResponse;
import in.koreatech.koin.domain.timetable.dto.TimetableUpdateRequest;
import in.koreatech.koin.domain.timetable.exception.SemesterNotFoundException;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetable.model.TimeTable;
import in.koreatech.koin.domain.timetable.model.TimetableFrame;
import in.koreatech.koin.domain.timetable.model.TimetableLecture;
import in.koreatech.koin.domain.timetable.repository.LectureRepository;
import in.koreatech.koin.domain.timetable.repository.SemesterRepository;
import in.koreatech.koin.domain.timetable.repository.TimeTableFrameRepository;
import in.koreatech.koin.domain.timetable.repository.TimeTableLectureRepository;
import in.koreatech.koin.domain.timetable.repository.TimeTableRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimetableService {

    private final LectureRepository lectureRepository;
    private final SemesterRepository semesterRepository;
    private final TimeTableRepository timeTableRepository;
    private final TimeTableFrameRepository timetableFrameRepository;
    private final TimeTableLectureRepository timetableLectureRepository;

    public List<LectureResponse> getLecturesBySemester(String semester) {
        List<Lecture> lectures = lectureRepository.findBySemester(semester);
        if (lectures.isEmpty()) {
            throw SemesterNotFoundException.withDetail(semester);
        }
        return lectures.stream()
            .map(LectureResponse::from)
            .toList();
    }

    /*
    public TimetableResponse getTimeTables(Integer userId, String semesterRequest) {
        Semester semester = semesterRepository.getBySemester(semesterRequest);
        return getTimeTableResponse(userId, semester);
    }

    @Transactional
    public TimetableResponse createTimeTables(Integer userId, TimeTableCreateRequest request) {
        User user = userRepository.getById(userId);
        Semester semester = semesterRepository.getBySemester(request.semester());
        for (TimeTableCreateRequest.InnerTimeTableRequest timeTableRequest : request.timetable()) {
            TimeTable timeTable = timeTableRequest.toTimeTable(user, semester);
            timeTableRepository.save(timeTable);
        }
        return getTimeTableResponse(userId, semester);
    }

     */
    @Transactional
    public TimetableResponse updateTimetables(Integer userId, TimetableUpdateRequest request) {
        Semester semester = semesterRepository.getBySemester(request.semester());
        TimetableFrame timetableFrame = timetableFrameRepository.getByUserIdAndSemesterId(userId,
            semester.getId(), true);

        for (TimetableUpdateRequest.InnerTimetableRequest timetableRequest : request.timetable()) {
            TimetableLecture timetableLecture;
            if (timetableRequest.id() != null) {
                timetableLecture = timetableLectureRepository.getById(timetableRequest.id());
            } else {
                timetableLecture = TimetableLecture.builder()
                    .className(timetableRequest.classTitle())
                    .classTime(timetableRequest.classTime().toString())
                    .classPlace(timetableRequest.classPlace())
                    .professor(timetableRequest.professor())
                    .memo(timetableRequest.memo())
                    .timetableFrame(timetableFrame)
                    .isDeleted(false)
                    .build();
            }
            timetableLecture.update(timetableRequest);

            timetableLectureRepository.save(timetableLecture);
        }
        return getTimetableResponse(userId, timetableFrame);
    }

    @Transactional
    public TimetableLectureResponse updateTimetablesLectures(Integer userId, TimetableLectureUpdateRequest request) {
        TimetableFrame timetableFrame = timetableFrameRepository.getById(request.id());
        for (TimetableLectureUpdateRequest.InnerTimetableLectureRequest timetableRequest : request.timetableLecture()) {
            TimetableLecture timetableLecture;
            if (timetableRequest.id() != null) {
                timetableLecture = timetableLectureRepository.getById(timetableRequest.id());
            } else {
                timetableLecture = TimetableLecture.builder()
                    .className(timetableRequest.classTitle())
                    .classTime(timetableRequest.classTime().toString())
                    .classPlace(timetableRequest.classPlace())
                    .professor(timetableRequest.professor())
                    .memo(timetableRequest.memo())
                    .timetableFrame(timetableFrame)
                    .isDeleted(false)
                    .build();
            }
            timetableLecture.update(timetableRequest);
            timetableLectureRepository.save(timetableLecture);
        }
        return getTimetableLectureResponse(userId, timetableFrame);
    }

    @Transactional
    public void deleteTimeTable(Integer id) {
        TimeTable timeTable = timeTableRepository.getById(id);
        timeTable.updateIsDeleted(true);
    }

    private TimetableResponse getTimetableResponse(Integer userId, TimetableFrame timetableFrame) {
        List<TimetableLecture> timetableLectures = timetableLectureRepository.findAllByTimetableFrameId(
            timetableFrame.getId());
        Integer grades = timetableLectures.stream()
            .mapToInt(timetableLecture -> {
                if (timetableLecture.getLecture() != null) {
                    return Integer.parseInt(timetableLecture.getLecture().getGrades());
                } else {
                    return 0;
                }
            })
            .sum();

        Integer totalGrades = calculateTotalGradesTimetable(userId);

        return TimetableResponse.of(timetableLectures, timetableFrame, grades, totalGrades);
    }

    private TimetableLectureResponse getTimetableLectureResponse(Integer userId, TimetableFrame timetableFrame) {
        List<TimetableLecture> timetableLectures = timetableLectureRepository.findAllByTimetableFrameId(
            timetableFrame.getId());
        List<TimetableFrame> timetableFrames = timetableFrameRepository.findAllByUserId(userId);
        Integer grades = timetableLectures.stream()
            .mapToInt(timetableLecture -> {
                if (timetableLecture.getLecture() != null) {
                    return Integer.parseInt(timetableLecture.getLecture().getGrades());
                } else {
                    return 0;
                }
            })
            .sum();

        Integer totalGrades = calculateTotalGradesTimetableLecture(timetableFrames);

        return TimetableLectureResponse.of(timetableFrame.getId(), timetableLectures, grades, totalGrades);
    }

    private int calculateTotalGradesTimetable(Integer userId) {
        int totalGrades = 0;
        List<TimetableFrame> timetableFrames = timetableFrameRepository.findAllByUserId(userId);

        for (TimetableFrame timetableFrame : timetableFrames) {
            if (timetableFrame.isMain()) {
                List<TimetableLecture> timetableLectures = timetableLectureRepository.findAllByTimetableFrameId(
                    timetableFrame.getId());
                for (TimetableLecture timetableLecture : timetableLectures) {
                    if (timetableLecture.getLecture() != null) {
                        totalGrades += Integer.parseInt(timetableLecture.getLecture().getGrades());
                    }
                }
            }
        }
        return totalGrades;
    }

    private int calculateTotalGradesTimetableLecture(List<TimetableFrame> timetableFrames) {
        int totalGrades = 0;
        for (TimetableFrame timeTableFrame : timetableFrames) {
            if (timeTableFrame.isMain()) {
                List<TimetableLecture> timetableLectures = timetableLectureRepository.findAllByTimetableFrameId(
                    timeTableFrame.getId());
                for (TimetableLecture timeTableLecture : timetableLectures) {
                    if (timeTableLecture.getLecture() != null) {
                        totalGrades += Integer.parseInt(timeTableLecture.getLecture().getGrades());
                    }
                }
            }
        }
        return totalGrades;
    }
}
