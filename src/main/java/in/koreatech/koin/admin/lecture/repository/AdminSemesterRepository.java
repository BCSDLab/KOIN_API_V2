package in.koreatech.koin.admin.lecture.repository;

import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_SEMESTER;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetableV3.model.Term;
import in.koreatech.koin.global.exception.CustomException;

public interface AdminSemesterRepository extends Repository<Semester, Integer> {

    Optional<Semester> findByYearAndTerm(Integer year, Term term);

    default Semester getByYearAndTerm(Integer year, Term term) {
        return findByYearAndTerm(year, term)
            .orElseThrow(() -> CustomException.of(
                NOT_FOUND_SEMESTER,
                "year: " + year + ", term: " + term.getDescription()
            ));
    }
}
