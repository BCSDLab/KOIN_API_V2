package in.koreatech.koin.admin.version.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.version.exception.VersionTypeNotFoundException;
import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.model.VersionType;
import in.koreatech.koin.global.marker.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface AdminVersionRepository extends Repository<Version, Integer> {

    Version save(Version request);

    Integer countByIsPrevious(boolean isPrevious);

    Integer countByType(String type);

    Page<Version> findAllByIsPrevious(boolean isPrevious, Pageable pageable);

    Page<Version> findAllByTypeOrderByVersionDesc(String type, PageRequest pageRequest);

    Optional<Version> findById(Integer id);

    default Version getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> VersionTypeNotFoundException.withDetail("id: " + id));
    }

    Optional<Version> findByTypeAndIsPrevious(String type, boolean isPrevious);

    default Version getByTypeAndIsPrevious(VersionType type, boolean isPrevious) {
        return findByTypeAndIsPrevious(type.getValue(), isPrevious)
            .orElseThrow(() -> VersionTypeNotFoundException.withDetail("versionType: " + type));
    }
}
