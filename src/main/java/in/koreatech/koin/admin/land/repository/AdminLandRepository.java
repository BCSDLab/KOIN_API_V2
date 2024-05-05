package in.koreatech.koin.admin.land.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.admin.land.execption.LandNameDuplicationException;
import in.koreatech.koin.domain.land.model.Land;

public interface AdminLandRepository extends Repository<Land, Integer> {

    Page<Land> findAllByIsDeleted(boolean isDeleted, Pageable pageable);

    Integer countAllByIsDeleted(boolean isDeleted);

    Land save(Land request);

    Optional<Land> findByName(String name);

    default Land getByName(String name) {
        Optional<Land> land = findByName(name);
        if (land.isPresent()) {
            throw LandNameDuplicationException.withDetail(name);
        }
        return land.orElse(null);
    }
}
