package in.koreatech.koin.domain.land.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.land.exception.LandNotFoundException;
import in.koreatech.koin.domain.land.model.Land;

public interface LandRepository extends Repository<Land, Long> {

    List<Land> findAll();

    Optional<Land> findById(Long id);

    Land save(Land request);

    default Land getById(Long id) {
        return findById(id).orElseThrow(() -> LandNotFoundException.withDetail("id: " + id));
    }
}
