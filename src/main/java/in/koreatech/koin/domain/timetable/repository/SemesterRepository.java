package in.koreatech.koin.domain.timetable.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.exception.SemesterNotFoundException;
import in.koreatech.koin.domain.timetable.model.Semester;

public interface SemesterRepository extends Repository<Semester, Long> {

    List<Semester> findAll();

    Semester save(Semester semester);

    Optional<Semester> findBySemester(String semester);

    default Semester getBySemester(String semester){
        return findBySemester(semester)
            .orElseThrow(() -> SemesterNotFoundException.withDetail("semester: " + semester));
    }
}
