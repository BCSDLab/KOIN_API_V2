package in.koreatech.koin.admin.lecture.service;

import static in.koreatech.koin.global.code.ApiResponseCode.DUPLICATE_LECTURE;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_SEMESTER;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.lecture.dto.AdminLectureCreateRequest;
import in.koreatech.koin.admin.lecture.repository.AdminLectureRepository;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetableV3.model.Term;
import in.koreatech.koin.domain.timetableV3.repository.SemesterRepositoryV3;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminLectureService {

    private final AdminLectureRepository adminLectureRepository;
    private final SemesterRepositoryV3 semesterRepositoryV3;

    @Transactional
    public void createLecture(AdminLectureCreateRequest request) {
        Term term = Term.fromDescription(request.term());
        Semester semester = semesterRepositoryV3.findByYearAndTerm(request.year(), term)
            .orElseThrow(() -> CustomException.of(
                NOT_FOUND_SEMESTER,
                "year: " + request.year() + ", term: " + request.term()
            ));

        if (adminLectureRepository.existsBySemesterAndCodeAndLectureClass(
            semester.getSemester(), request.code(), request.lectureClass()
        )) {
            throw CustomException.of(
                DUPLICATE_LECTURE,
                "semester: " + semester.getSemester() + ", code: " + request.code()
                    + ", lectureClass: " + request.lectureClass()
            );
        }

        Lecture lecture = request.toEntity(semester.getSemester());
        adminLectureRepository.save(lecture);
    }
}
