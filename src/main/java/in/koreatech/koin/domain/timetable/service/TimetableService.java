package in.koreatech.koin.domain.timetable.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.timetable.dto.TimeTableCreateRequest;
import in.koreatech.koin.domain.timetable.dto.TimeTableCreateRequest.InnerTimeTableRequest;
import in.koreatech.koin.domain.timetable.dto.TimetableLectureCreateRequest;
import in.koreatech.koin.domain.timetable.dto.TimetableLectureResponse;
import in.koreatech.koin.domain.timetable.dto.TimetableResponse;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetable.model.TimetableFrame;
import in.koreatech.koin.domain.timetable.model.TimetableLecture;
import in.koreatech.koin.domain.timetable.repository.LectureRepository;
import in.koreatech.koin.domain.timetable.repository.SemesterRepository;
import in.koreatech.koin.domain.timetable.repository.TimetableFrameRepository;
import in.koreatech.koin.domain.timetable.repository.TimetableLectureRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimetableService {

    private final LectureRepository lectureRepository;
    private final TimetableLectureRepository timetableLectureRepository;
    private final TimetableFrameRepository timetableFrameRepository;
    private final SemesterRepository semesterRepository;

    @Transactional
    public TimetableResponse createTimeTables(Integer userId, TimeTableCreateRequest request) {
        Semester semester = semesterRepository.getBySemester(request.semester());
        List<TimetableLecture> timetableLectures = new ArrayList<>();
        TimetableFrame TimetableFrame = timetableFrameRepository.getByUserIdAndSemester(userId, semester.getId(), true);

        for(InnerTimeTableRequest timeTable : request.timetable()) {
            Lecture lecture = lectureRepository.getBySemesterAndNameAndLectureClass(request.semester(), timeTable.classTitle(), timeTable.lectureClass());
            TimetableLecture timetableLecture = TimetableLecture.builder()
                .className(timeTable.classTitle())
                .classTime(timeTable.classTime().toString())
                .classPlace(timeTable.classPlace())
                .professor(timeTable.professor())
                .memo(timeTable.memo())
                .lecture(lecture)
                .timetableFrame(TimetableFrame)
                .build();

            timetableLectures.add(timetableLectureRepository.save(timetableLecture));
        }

        return getTimeTableResponse(userId, TimetableFrame, timetableLectures);
    }

    private TimetableResponse getTimeTableResponse(Integer userId, TimetableFrame timetableFrame, List<TimetableLecture> timetableLectures) {
        int grades = 0;
        int totalGrades = 0;

        if (timetableFrame.isMain()) {
            grades = timetableLectures.stream()
                .filter(lecture -> lecture.getLecture() != null)
                .mapToInt(lecture -> Integer.parseInt(lecture.getLecture().getGrades()))
                .sum();
        }
        for (TimetableFrame timetableFrames : timetableFrameRepository.findAllByUserIdAndIsMain(userId, true)) {
            totalGrades += timetableLectureRepository.findAllByTimetableFrameId(timetableFrames.getId()).stream()
                .filter(lecture -> lecture.getLecture() != null)
                .mapToInt(lecture -> Integer.parseInt(lecture.getLecture().getGrades()))
                .sum();
        }

        return TimetableResponse.of(timetableLectures, timetableFrame, grades, totalGrades);
    }

    @Transactional
    public TimetableLectureResponse createTimetableLectures(Integer userId, TimetableLectureCreateRequest request) {
        TimetableFrame timetableFrame = timetableFrameRepository.getById(request.timetableFrameId());
        for (TimetableLectureCreateRequest.InnerTimeTableLectureRequest timetableLectureRequest : request.timetableLecture()) {
            Lecture lecture = lectureRepository.getLectureById(timetableLectureRequest.lectureId());
            TimetableLecture timetableLecture = timetableLectureRequest.toTimetableLecture(timetableFrame, lecture);
            timetableLectureRepository.save(timetableLecture);
        }

        List<TimetableLecture> timetableLectures = timetableLectureRepository.findAllByTimetableFrameId(timetableFrame.getId());
        return getTimetableLectureResponse(userId, timetableFrame, timetableLectures);
    }

    private TimetableLectureResponse getTimetableLectureResponse(Integer userId, TimetableFrame timetableFrame, List<TimetableLecture> timetableLectures) {
        int grades = 0;
        int totalGrades = 0;

        if (timetableFrame.isMain()) {
            grades = timetableLectures.stream()
                .filter(lecture -> lecture.getLecture() != null)
                .mapToInt(lecture -> Integer.parseInt(lecture.getLecture().getGrades()))
                .sum();
        }

        for (TimetableFrame timetableFrames : timetableFrameRepository.findAllByUserIdAndIsMain(userId, true)) {
            totalGrades += timetableLectureRepository.findAllByTimetableFrameId(timetableFrames.getId()).stream()
                .filter(lecture -> lecture.getLecture() != null)
                .mapToInt(lecture -> Integer.parseInt(lecture.getLecture().getGrades()))
                .sum();
        }

        return TimetableLectureResponse.of(timetableFrame.getId(), timetableLectures, grades, totalGrades);
    }
}
