package in.koreatech.koin.admin.club.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.model.Criteria;
import in.koreatech.koin.admin.club.dto.ClubAdminCondition;
import in.koreatech.koin.admin.club.dto.request.ChangeAdminClubActiveRequest;
import in.koreatech.koin.admin.club.dto.request.CreateAdminClubRequest;
import in.koreatech.koin.admin.club.dto.request.DecideAdminClubAdminRequest;
import in.koreatech.koin.admin.club.dto.request.ModifyAdminClubRequest;
import in.koreatech.koin.admin.club.dto.response.AdminClubAdminsResponse;
import in.koreatech.koin.admin.club.dto.response.AdminClubResponse;
import in.koreatech.koin.admin.club.dto.response.AdminClubsResponse;
import in.koreatech.koin.admin.club.repository.AdminClubAdminRepository;
import in.koreatech.koin.admin.club.repository.AdminClubCategoryRepository;
import in.koreatech.koin.admin.club.repository.AdminClubRepository;
import in.koreatech.koin.admin.user.repository.AdminUserRepository;
import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubAdmin;
import in.koreatech.koin.domain.club.model.ClubCategory;
import in.koreatech.koin.domain.club.repository.ClubRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminClubService {

    private final ClubRepository clubRepository;
    private final AdminClubCategoryRepository adminClubCategoryRepository;
    private final AdminClubAdminRepository adminClubAdminRepository;
    private final AdminClubRepository adminClubRepository;
    private final AdminUserRepository adminUserRepository;

    public AdminClubsResponse getClubs(Integer page, Integer limit, Boolean sortByLike, Integer clubCategoryId) {
        boolean hasCategory = clubCategoryId != null;

        if (hasCategory) {
            adminClubCategoryRepository.getById(clubCategoryId);
        }

        Integer total = hasCategory
            ? adminClubRepository.countByClubCategoryId(clubCategoryId)
            : adminClubRepository.count();

        Criteria criteria = Criteria.of(page, limit, total);

        Sort sort = sortByLike
            ? Sort.by(Sort.Direction.DESC, "likes")
            : Sort.by(Sort.Direction.DESC, "created_at");

        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(), sort);

        Page<Club> clubs = hasCategory
            ? adminClubRepository.findAllByClubCategoryId(clubCategoryId, pageRequest)
            : adminClubRepository.findAll(pageRequest);

        return AdminClubsResponse.from(clubs);
    }

    public AdminClubResponse getClub(Integer clubId) {
        Club club = adminClubRepository.getById(clubId);
        return AdminClubResponse.from(club);
    }

    @Transactional
    public void createClub(CreateAdminClubRequest request) {
        ClubCategory clubCategory = adminClubCategoryRepository.getById(request.clubCategoryId());
        Club club = adminClubRepository.save(request.toEntity(clubCategory));

        List<ClubAdmin> clubAdmins = request.clubAdmins().stream()
            .map(innerClubAdminRequest ->
                innerClubAdminRequest.toEntity(club, adminUserRepository.getByUserId(innerClubAdminRequest.userid()))
            )
            .toList();

        adminClubAdminRepository.saveAll(clubAdmins);
    }

    @Transactional
    public void modifyClub(Integer clubId, ModifyAdminClubRequest request) {
        ClubCategory clubCategory = adminClubCategoryRepository.getById(request.clubCategoryId());
        Club club = adminClubRepository.getById(clubId);

        List<ClubAdmin> clubAdmins = request.clubAdmins().stream()
            .map(innerClubAdminUpdateRequest ->
                innerClubAdminUpdateRequest.toEntity(club,
                    adminUserRepository.getByUserId(innerClubAdminUpdateRequest.userid()))
            )
            .toList();

        club.modifyClub(request.name(),
            request.imageUrl(),
            clubCategory,
            request.location(),
            request.description(),
            request.active()
        );

        adminClubAdminRepository.deleteAllByClub(club);
        adminClubAdminRepository.saveAll(clubAdmins);
    }

    @Transactional
    public void changeActive(Integer clubId, ChangeAdminClubActiveRequest request) {
        Club club = clubRepository.getById(clubId);
        club.updateActive(request.isActive());
    }

    @Transactional
    public void decideClubAdmin(Integer clubId, DecideAdminClubAdminRequest request) {
        if (request.isAccept()) {
            ClubAdmin clubAdmin = adminClubAdminRepository.findFirstAcceptedByClubId(clubId);
            clubAdmin.acceptClubAdmin();
        } else {
            clubRepository.deleteById(clubId);
        }
    }

    public AdminClubAdminsResponse getClubAdmins(ClubAdminCondition condition) {
        int totalCount = adminClubRepository.countUnacceptedAll();
        Criteria criteria = Criteria.of(condition.page(), condition.limit(), totalCount);
        Sort.Direction direction = condition.getDirection();

        Page<ClubAdmin> result = getClubAdminsResultPage(criteria, direction);

        return AdminClubAdminsResponse.of(result, criteria);
    }

    public AdminClubAdminsResponse getUnacceptedClubAdmins(ClubAdminCondition condition) {
        int totalCount = adminClubRepository.countUnacceptedAll();
        Criteria criteria = Criteria.of(condition.page(), condition.limit(), totalCount);
        Sort.Direction direction = condition.getDirection();

        Page<ClubAdmin> result = getUnacceptedClubAdminsResultPage(criteria, direction);

        return AdminClubAdminsResponse.of(result, criteria);
    }

    private Page<ClubAdmin> getClubAdminsResultPage(Criteria criteria, Sort.Direction direction) {
        PageRequest pageRequest = PageRequest.of(
            criteria.getPage(),
            criteria.getLimit(),
            Sort.by(direction, "club.createdAt")
        );

        return adminClubRepository.findAcceptedPageAll(pageRequest);
    }

    private Page<ClubAdmin> getUnacceptedClubAdminsResultPage(Criteria criteria, Sort.Direction direction) {
        PageRequest pageRequest = PageRequest.of(
            criteria.getPage(),
            criteria.getLimit(),
            Sort.by(direction, "club.createdAt")
        );

        return adminClubRepository.findUnacceptedPageAll(pageRequest);
    }
}
