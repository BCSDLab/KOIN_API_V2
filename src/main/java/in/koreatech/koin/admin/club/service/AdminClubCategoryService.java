package in.koreatech.koin.admin.club.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.club.dto.response.AdminClubCategoriesResponse;
import in.koreatech.koin.admin.club.repository.AdminClubCategoryRepository;
import in.koreatech.koin.domain.club.model.ClubCategory;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminClubCategoryService {

    private final AdminClubCategoryRepository adminClubCategoryRepository;

    public AdminClubCategoriesResponse getClubCategories() {
        List<ClubCategory> clubCategories = adminClubCategoryRepository.findAll();
        return AdminClubCategoriesResponse.of(clubCategories);
    }
}
