package in.koreatech.koin.admin.semester.service;

import static in.koreatech.koin.global.code.ApiResponseCode.*;

import java.time.LocalDate;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.semester.dto.AdminSemesterCreateRequest;
import in.koreatech.koin.admin.semester.repository.AdminCoopShopSemesterRepository;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCoopShopSemesterService {

    private static final Pattern SEMESTER_PATTERN = Pattern.compile("^\\d{2}-.+$");

    private final AdminCoopShopSemesterRepository adminCoopShopSemesterRepository;

    @Transactional
    public void createCoopshopSemester(AdminSemesterCreateRequest request) {
        validateSemesterFormat(request.semester());
        validateDuplicateSemester(request.semester());
        validateDateRange(request.fromDate(), request.toDate());
        validateOverlappingDateRange(request.fromDate(), request.toDate());

        adminCoopShopSemesterRepository.save(request.toEntity());
    }

    private void validateSemesterFormat(String semester) {
        if (!SEMESTER_PATTERN.matcher(semester).matches()) {
            throw CustomException.of(INVALID_SEMESTER_FORMAT);
        }
    }

    private void validateDuplicateSemester(String semester) {
        if (adminCoopShopSemesterRepository.findBySemester(semester).isPresent()) {
            throw CustomException.of(DUPLICATE_SEMESTER);
        }
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate.isAfter(toDate)) {
            throw CustomException.of(INVALID_START_DATE_AFTER_END_DATE);
        }
    }

    private void validateOverlappingDateRange(LocalDate fromDate, LocalDate toDate) {
        if (adminCoopShopSemesterRepository.existsOverlappingDateRange(fromDate, toDate)) {
            throw CustomException.of(OVERLAPPING_SEMESTER_DATE_RANGE);
        }
    }
}
