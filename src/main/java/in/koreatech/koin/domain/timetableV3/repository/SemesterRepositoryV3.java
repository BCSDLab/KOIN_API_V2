package in.koreatech.koin.domain.timetableV3.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetableV3.model.SemesterV3;

public interface SemesterRepositoryV3 extends Repository<SemesterV3, Integer> {

    List<SemesterV3> findAll();
}