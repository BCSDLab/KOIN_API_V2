package in.koreatech.koin.admin.semester.service;

import static in.koreatech.koin.global.code.ApiResponseCode.DUPLICATE_SEMESTER;
import static in.koreatech.koin.global.code.ApiResponseCode.OVERLAPPING_SEMESTER_DATE_RANGE;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.semester.dto.AdminSemesterCreateRequest;
import in.koreatech.koin.admin.semester.dto.AdminSemesterResponse;
import in.koreatech.koin.admin.semester.repository.AdminCoopShopSemesterRepository;
import in.koreatech.koin.domain.coopshop.model.CoopSemester;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCoopShopSemesterService {

    private final AdminCoopShopSemesterRepository adminCoopShopSemesterRepository;

    @Transactional
    public void createCoopshopSemester(AdminSemesterCreateRequest request) {
        validateDuplicateSemester(request.semester());
        validateOverlappingDateRange(request.fromDate(), request.toDate());
        CoopSemester coopSemester = CoopSemester.of(request.semester(), request.fromDate(), request.toDate());

        adminCoopShopSemesterRepository.save(coopSemester);
    }

    public List<AdminSemesterResponse> getCoopshopSemesters() {
        List<CoopSemester> coopSemesters = adminCoopShopSemesterRepository.findAllByOrderByFromDateDesc();
        return coopSemesters.stream()
            .map(AdminSemesterResponse::from)
            .toList();
    }

    private void validateDuplicateSemester(String semester) {
        if (adminCoopShopSemesterRepository.findBySemester(semester).isPresent()) {
            throw CustomException.of(DUPLICATE_SEMESTER);
        }
    }

    private void validateOverlappingDateRange(LocalDate fromDate, LocalDate toDate) {
        if (adminCoopShopSemesterRepository.existsOverlappingDateRange(fromDate, toDate)) {
            throw CustomException.of(OVERLAPPING_SEMESTER_DATE_RANGE);
        }
    }
}
