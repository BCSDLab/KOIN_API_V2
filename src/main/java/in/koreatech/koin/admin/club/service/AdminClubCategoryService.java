package in.koreatech.koin.admin.club.service;

import in.koreatech.koin.admin.club.dto.response.AdminClubCategoriesResponse;
import in.koreatech.koin.admin.club.repository.AdminClubCategoryRepository;
import in.koreatech.koin.domain.club.category.model.ClubCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminClubCategoryService {

    private final AdminClubCategoryRepository adminClubCategoryRepository;

    public AdminClubCategoriesResponse getClubCategories() {
        List<ClubCategory> clubCategories = adminClubCategoryRepository.findAll();
        return AdminClubCategoriesResponse.from(clubCategories);
    }
}
