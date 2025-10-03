package in.koreatech.koin.domain.land.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.land.exception.LandNotFoundException;
import in.koreatech.koin.domain.land.model.Land;
import in.koreatech.koin.global.config.repository.JpaRepository;

@JpaRepository
public interface LandRepository extends Repository<Land, Integer> {

    List<Land> findAll();

    Optional<Land> findById(Integer id);

    Land save(Land request);

    default Land getById(Integer id) {
        return findById(id).orElseThrow(() -> LandNotFoundException.withDetail("id: " + id));
    }
}
