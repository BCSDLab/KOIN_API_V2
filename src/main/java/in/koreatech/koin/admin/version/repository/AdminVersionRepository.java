package in.koreatech.koin.admin.version.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.version.exception.VersionTypeNotFoundException;
import in.koreatech.koin.domain.version.model.Version;

public interface AdminVersionRepository extends Repository<Version, Integer> {

    Version save(Version request);

    Optional<Version> findByType(String Type);

    Integer countAll();

    Page<Version> findAll(Pageable pageable);

    default Version getByType(String type) {
        return findByType(type).orElseThrow(() -> VersionTypeNotFoundException.withDetail("type: " + type));
    }

    Optional<Version> findById(Integer id);

    default Version getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> VersionTypeNotFoundException.withDetail("id: " + id));
    }

    Page<Version> findAllByType(String type, PageRequest pageRequest);
}
