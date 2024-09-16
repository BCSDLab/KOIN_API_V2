package in.koreatech.koin.admin.version.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.version.exception.VersionTypeNotFoundException;
import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.model.VersionHistory;

public interface AdminVersionHistoryRepository extends Repository<VersionHistory, Integer> {

    VersionHistory save(Version request);

    Optional<VersionHistory> findByType(String Type);

    Integer countAll();

    Page<VersionHistory> findAll(Pageable pageable);

    default VersionHistory getByType(String type) {
        return findByType(type).orElseThrow(() -> VersionTypeNotFoundException.withDetail("type: " + type));
    }

    Optional<VersionHistory> findById(Integer id);

    default VersionHistory getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> VersionTypeNotFoundException.withDetail("id: " + id));
    }

    Page<VersionHistory> findAllByType(String type, PageRequest pageRequest);
}
