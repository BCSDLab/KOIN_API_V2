package in.koreatech.koin.admin.club.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.club.model.ClubCategory;

public interface AdminClubCategoryRepository extends Repository<ClubCategory, Integer> {
    List<ClubCategory> findAll();
}
