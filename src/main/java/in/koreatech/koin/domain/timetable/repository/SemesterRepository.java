package in.koreatech.koin.domain.timetable.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.model.Semester;

public interface SemesterRepository extends Repository<Semester, Long> {

    List<Semester> findAll();

    Semester save(Semester semester);
}
