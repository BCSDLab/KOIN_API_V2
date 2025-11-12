package in.koreatech.koin.domain.timetableV2.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.exception.SemesterNotFoundException;
import in.koreatech.koin.domain.timetable.model.Semester;

public interface SemesterRepositoryV2 extends Repository<Semester, Integer> {

    Semester save(Semester semester);

    Optional<Semester> findBySemester(String semester);

    default Semester getBySemester(String semester) {
        return findBySemester(semester)
            .orElseThrow(() -> SemesterNotFoundException.withDetail("semester: " + semester));
    }
}
