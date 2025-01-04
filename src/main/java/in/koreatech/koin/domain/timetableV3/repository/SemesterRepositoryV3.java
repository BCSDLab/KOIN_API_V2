package in.koreatech.koin.domain.timetableV3.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.model.Semester;

public interface SemesterRepositoryV3 extends Repository<Semester, Integer> {

    List<Semester> findAll();

    Semester save(Semester semester);
}
