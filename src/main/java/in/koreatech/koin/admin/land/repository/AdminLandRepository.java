package in.koreatech.koin.admin.land.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.land.exception.LandNotFoundException;
import in.koreatech.koin.domain.land.model.Land;
import in.koreatech.koin.config.repository.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface AdminLandRepository extends Repository<Land, Integer> {

    Page<Land> findAllByIsDeleted(boolean isDeleted, Pageable pageable);

    Integer countAllByIsDeleted(boolean isDeleted);

    Land save(Land request);

    Optional<Land> findByName(String name);

    default Land getByName(String name) {
        return findByName(name).orElseThrow(() -> LandNotFoundException.withDetail("name: " + name));
    }

    Optional<Land> findById(Integer id);

    default Land getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> LandNotFoundException.withDetail("id: " + id));
    }

}
