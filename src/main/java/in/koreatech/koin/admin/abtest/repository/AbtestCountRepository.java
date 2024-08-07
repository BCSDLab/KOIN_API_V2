package in.koreatech.koin.admin.abtest.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.admin.abtest.model.AbtestCount;

public interface AbtestCountRepository extends Repository<AbtestCount, Integer> {
    List<AbtestCount> findAll();
}
