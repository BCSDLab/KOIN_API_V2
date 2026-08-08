package in.koreatech.koin.admin.semester.service;

import static in.koreatech.koin.global.code.ApiResponseCode.DUPLICATE_SEMESTER;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.semester.dto.AdminTimetableSemesterCreateRequest;
import in.koreatech.koin.admin.semester.repository.AdminTimetableSemesterRepository;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminTimetableSemesterService {

    private final AdminTimetableSemesterRepository adminTimetableSemesterRepository;

    @Transactional
    public void createSemester(AdminTimetableSemesterCreateRequest request) {
        Semester semester = Semester.of(request.year(), request.term());
        validateDuplicateSemester(semester);
        adminTimetableSemesterRepository.save(semester);
    }

    private void validateDuplicateSemester(Semester semester) {
        if (adminTimetableSemesterRepository.existsBySemesterOrYearAndTerm(
            semester.getSemester(), semester.getYear(), semester.getTerm()
        )) {
            throw CustomException.of(DUPLICATE_SEMESTER);
        }
    }
}
