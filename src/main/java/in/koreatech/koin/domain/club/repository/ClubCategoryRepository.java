package in.koreatech.koin.domain.club.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.admin.club.exception.ClubCategoryNotFound;
import in.koreatech.koin.domain.club.model.ClubCategory;

public interface ClubCategoryRepository extends Repository<ClubCategory, Integer> {

    List<ClubCategory> findAll();

    Optional<ClubCategory> findByName(String name);

    default ClubCategory getByName(String name) {
        return findByName(name)
            .orElseThrow(() -> ClubCategoryNotFound.withDetail("name : " + name));
    }

    ClubCategory save(ClubCategory category);

    Optional<ClubCategory> findById(Integer id);

    default ClubCategory getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> ClubCategoryNotFound.withDetail("id : " + id));
    }
}
