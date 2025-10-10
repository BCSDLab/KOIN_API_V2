package in.koreatech.koin.domain.timetableV3.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.exception.SemesterNotFoundException;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetableV3.model.Term;
import in.koreatech.koin.global.marker.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface SemesterRepositoryV3 extends Repository<Semester, Integer> {

    List<Semester> findAll();

    Optional<Semester> findBySemester(String semester);

    Semester save(Semester semester);

    Optional<Semester> findByYearAndTerm(Integer year, Term term);

    default Semester getByYearAndTerm(Integer year, Term term) {
        return findByYearAndTerm(year, term)
            .orElseThrow(
                () -> SemesterNotFoundException.withDetail("year : " + year + "term : " + term.getDescription()));
    }

    default Semester getBySemester(String semester) {
        return findBySemester(semester)
            .orElseThrow(() -> SemesterNotFoundException.withDetail("semester : " + semester));
    }
}
