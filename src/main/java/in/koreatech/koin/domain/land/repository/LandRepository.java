package in.koreatech.koin.domain.land.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.land.domain.Land;

public interface LandRepository extends Repository<Land, Long> {

    List<Land> findAll();
}
