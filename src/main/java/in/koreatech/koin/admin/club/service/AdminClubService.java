package in.koreatech.koin.admin.club.service;

import static in.koreatech.koin.domain.club.enums.SNSType.*;

import java.util.AbstractMap;
import java.util.List;
import java.util.stream.Stream;

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

    public AdminClubsResponse getClubs(Integer page, Integer limit, Integer clubCategoryId) {
        boolean hasCategory = clubCategoryId != null;

        if (hasCategory) {
            adminClubCategoryRepository.getById(clubCategoryId);
        }

        Integer total = hasCategory
            ? adminClubRepository.countByClubCategoryId(clubCategoryId)
            : adminClubRepository.count();

        Criteria criteria = Criteria.of(page, limit, total);

        Sort sort = Sort.by(Sort.Direction.DESC, "created_at");

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

        List<ClubSNS> clubSNSs = Stream.of(
                new AbstractMap.SimpleEntry<>(INSTAGRAM, request.instagram()),
                new AbstractMap.SimpleEntry<>(GOOGLE_FORM, request.googleForm()),
                new AbstractMap.SimpleEntry<>(PHONE_NUMBER, request.phoneNumber()),
                new AbstractMap.SimpleEntry<>(OPEN_CHAT, request.openChat())
            )
            .filter(entry -> entry.getValue() != null)
            .map(entry -> new ClubSNS(club, entry.getKey(), entry.getValue()))
            .toList();

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

        List<ClubSNS> clubSNSs = Stream.of(
                new AbstractMap.SimpleEntry<>(INSTAGRAM, request.instagram()),
                new AbstractMap.SimpleEntry<>(GOOGLE_FORM, request.googleForm()),
                new AbstractMap.SimpleEntry<>(PHONE_NUMBER, request.phoneNumber()),
                new AbstractMap.SimpleEntry<>(OPEN_CHAT, request.openChat())
            )
            .filter(entry -> entry.getValue() != null)
            .map(entry -> new ClubSNS(club, entry.getKey(), entry.getValue()))
            .toList();

        club.modifyClub(request.name(),
            request.imageUrl(),
            clubCategory,
            request.location(),
            request.description(),
            request.active()
        );

        adminClubSnsRepository.deleteAllByClub(club);
        adminClubSnsRepository.saveAll(clubSNSs);

        adminClubAdminRepository.deleteAllByClub(club);
        adminClubAdminRepository.saveAll(clubAdmins);
    }
}
