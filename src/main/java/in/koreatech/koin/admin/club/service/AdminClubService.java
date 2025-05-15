package in.koreatech.koin.admin.club.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.model.Criteria;
import in.koreatech.koin.admin.club.dto.request.CreateAdminClubRequest;
import in.koreatech.koin.admin.club.dto.request.ModifyAdminClubRequest;
import in.koreatech.koin.admin.club.dto.response.AdminClubResponse;
import in.koreatech.koin.admin.club.dto.response.AdminClubsResponse;
import in.koreatech.koin.admin.club.repository.AdminClubAdminRepository;
import in.koreatech.koin.admin.club.repository.AdminClubCategoryRepository;
import in.koreatech.koin.admin.club.repository.AdminClubRepository;
import in.koreatech.koin.admin.club.repository.AdminClubSnsRepository;
import in.koreatech.koin.admin.user.repository.AdminUserRepository;
import in.koreatech.koin.domain.club.enums.SNSType;
import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubAdmin;
import in.koreatech.koin.domain.club.model.ClubCategory;
import in.koreatech.koin.domain.club.model.ClubSNS;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminClubService {

    private final AdminClubCategoryRepository adminClubCategoryRepository;
    private final AdminClubAdminRepository adminClubAdminRepository;
    private final AdminClubRepository adminClubRepository;
    private final AdminUserRepository adminUserRepository;
    private final AdminClubSnsRepository adminClubSnsRepository;

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

        List<ClubSNS> clubSNSs = List.of(
            new ClubSNS(club, SNSType.INSTAGRAM, request.instagram()),
            new ClubSNS(club, SNSType.GOOGLE_FORM, request.googleForm()),
            new ClubSNS(club, SNSType.PHONE_NUMBER, request.phoneNumber()),
            new ClubSNS(club, SNSType.OPEN_CHAT, request.openChat())
        );

        adminClubSnsRepository.saveAll(clubSNSs);
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
}
