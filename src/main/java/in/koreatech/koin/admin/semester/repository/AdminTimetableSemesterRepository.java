package in.koreatech.koin.admin.semester.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetableV3.model.Term;

public interface AdminTimetableSemesterRepository extends Repository<Semester, Integer> {

    boolean existsBySemesterOrYearAndTerm(String semester, Integer year, Term term);

    Semester save(Semester semester);
}
