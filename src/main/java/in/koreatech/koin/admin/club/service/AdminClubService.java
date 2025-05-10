package in.koreatech.koin.admin.club.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.model.Criteria;
import in.koreatech.koin.admin.club.dto.response.AdminClubResponse;
import in.koreatech.koin.admin.club.dto.response.AdminClubsResponse;
import in.koreatech.koin.admin.club.repository.AdminClubCategoryRepository;
import in.koreatech.koin.admin.club.repository.AdminClubRepository;
import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubCategory;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminClubService {

    private final AdminClubCategoryRepository adminClubCategoryRepository;
    private final AdminClubRepository adminClubRepository;

    public AdminClubsResponse getClubs(Integer page, Integer limit, Boolean sortByLike, String clubCategoryName) {
        ClubCategory clubCategory = adminClubCategoryRepository.getByName(clubCategoryName);
        Integer clubCategoryId = clubCategory.getId();
        Integer total = adminClubRepository.countByClubCategoryId(clubCategoryId);

        Criteria criteria = Criteria.of(page, limit, total);
        Sort sort = sortByLike ?
            Sort.by(Sort.Direction.DESC, "created_at", "likes") :
            Sort.by(Sort.Direction.DESC, "created_at");
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(), sort);

        Page<Club> clubs = adminClubRepository.findAllByClubCategoryId(clubCategoryId, pageRequest);

        return AdminClubsResponse.from(clubs);
    }

    public AdminClubResponse getClub(Integer clubId) {
        Club club = adminClubRepository.getById(clubId);
        return AdminClubResponse.from(club);
    }
}
