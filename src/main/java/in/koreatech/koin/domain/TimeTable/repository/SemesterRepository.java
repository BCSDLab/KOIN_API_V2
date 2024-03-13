package in.koreatech.koin.domain.TimeTable.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.TimeTable.model.Semester;

public interface SemesterRepository extends Repository<Semester, Long> {

    List<Semester> findAll();

    Semester save(Semester semester);

}
