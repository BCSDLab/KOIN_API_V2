package in.koreatech.koin.domain.club.category.service;

import in.koreatech.koin.domain.club.category.dto.response.ClubCategoriesResponse;
import in.koreatech.koin.domain.club.category.model.ClubCategory;
import in.koreatech.koin.domain.club.category.repository.ClubCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubCategoryService {

    private final ClubCategoryRepository clubCategoryRepository;

    public ClubCategoriesResponse getClubCategories() {
        List<ClubCategory> clubCategories = clubCategoryRepository.findAll();
        return ClubCategoriesResponse.from(clubCategories);
    }
}
