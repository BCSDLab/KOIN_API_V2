package in.koreatech.koin.admin.lecture.service;

import static in.koreatech.koin.global.code.ApiResponseCode.DUPLICATE_LECTURE;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_SEMESTER;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.lecture.dto.AdminLectureCreateRequest;
import in.koreatech.koin.admin.lecture.dto.AdminLectureCreateRequest.LectureRequest;
import in.koreatech.koin.admin.lecture.repository.AdminLectureRepository;
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
    public void createLectures(AdminLectureCreateRequest request) {
        Term term = Term.fromDescription(request.term());
        Semester semester = semesterRepositoryV3.findByYearAndTerm(request.year(), term)
            .orElseThrow(() -> CustomException.of(
                NOT_FOUND_SEMESTER,
                "year: " + request.year() + ", term: " + request.term()
            ));

        Set<LectureKey> lectureKeys = new HashSet<>();
        for (LectureRequest lecture : request.lectures()) {
            LectureKey lectureKey = new LectureKey(lecture.code(), lecture.lectureClass());
            if (!lectureKeys.add(lectureKey) || adminLectureRepository.existsBySemesterAndCodeAndLectureClass(
                semester.getSemester(), lecture.code(), lecture.lectureClass()
            )) {
                throw CustomException.of(
                    DUPLICATE_LECTURE,
                    "semester: " + semester.getSemester() + ", code: " + lecture.code()
                        + ", lectureClass: " + lecture.lectureClass()
                );
            }
        }

        adminLectureRepository.saveAll(request.toEntities(semester.getSemester()));
    }

    private record LectureKey(String code, String lectureClass) {
    }
}
