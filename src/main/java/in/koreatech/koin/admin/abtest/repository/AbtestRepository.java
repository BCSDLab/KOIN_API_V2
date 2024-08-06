package in.koreatech.koin.admin.abtest.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.admin.abtest.model.Abtest;

public interface AbtestRepository extends Repository<Abtest, Integer> {

    Optional<Abtest> findById(Integer id);
}
