package in.koreatech.koin.domain.club.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.club.dto.response.ClubCategoriesResponse;
import in.koreatech.koin.domain.club.model.ClubCategory;
import in.koreatech.koin.domain.club.repository.ClubCategoryRepository;
import lombok.RequiredArgsConstructor;

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
