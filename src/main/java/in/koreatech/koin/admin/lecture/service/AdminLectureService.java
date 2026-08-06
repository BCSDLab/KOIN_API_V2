package in.koreatech.koin.admin.lecture.service;

import static in.koreatech.koin.global.code.ApiResponseCode.DUPLICATE_LECTURE;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.lecture.dto.AdminLectureCreateRequest;
import in.koreatech.koin.admin.lecture.dto.AdminLectureCreateRequest.LectureRequest;
import in.koreatech.koin.admin.lecture.model.LectureKey;
import in.koreatech.koin.admin.lecture.repository.AdminLectureRepository;
import in.koreatech.koin.admin.lecture.repository.AdminSemesterRepository;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminLectureService {

    private final AdminLectureRepository adminLectureRepository;
    private final AdminSemesterRepository adminSemesterRepository;

    @Transactional
    public void createLectures(AdminLectureCreateRequest request) {
        Semester semester = adminSemesterRepository.getByYearAndTerm(request.year(), request.term());

        Set<LectureKey> lectureKeys = new HashSet<>();
        for (LectureRequest lecture : request.lectures()) {
            LectureKey lectureKey = LectureKey.of(lecture.code(), lecture.lectureClass());
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
}
