package in.koreatech.koin.admin.club.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import in.koreatech.koin.admin.club.dto.response.AdminNewClubResponse;
import in.koreatech.koin.admin.club.repository.AdminClubAdminRepository;
import in.koreatech.koin.admin.club.repository.AdminClubCategoryRepository;
import in.koreatech.koin.admin.club.repository.AdminClubRepository;
import in.koreatech.koin.admin.user.repository.AdminUserRepository;
import in.koreatech.koin.domain.club.exception.ClubNotFoundException;
import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubAdmin;
import in.koreatech.koin.domain.club.model.ClubCategory;
import in.koreatech.koin.domain.club.model.redis.ClubCreateRedis;
import in.koreatech.koin.domain.club.repository.ClubAdminRepository;
import in.koreatech.koin.domain.club.repository.ClubCategoryRepository;
import in.koreatech.koin.domain.club.repository.ClubRepository;
import in.koreatech.koin.domain.club.repository.redis.ClubCreateRedisRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminClubService {

    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final ClubAdminRepository clubAdminRepository;
    private final ClubCategoryRepository clubCategoryRepository;
    private final AdminClubCategoryRepository adminClubCategoryRepository;
    private final AdminClubAdminRepository adminClubAdminRepository;
    private final AdminClubRepository adminClubRepository;
    private final AdminUserRepository adminUserRepository;
    private final ClubCreateRedisRepository clubCreateRedisRepository;

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

    public AdminNewClubResponse getNewClub(String clubName) {
        ClubCreateRedis clubInformation = clubCreateRedisRepository.findById(clubName)
            .orElseThrow(() -> ClubNotFoundException.withDetail("신청한 동아리를 찾을 수 없습니다."));
        User requester = userRepository.getById(clubInformation.getRequesterId());
        ClubCategory clubCategory = clubCategoryRepository.getById(clubInformation.getClubCategoryId());

        return AdminNewClubResponse.from(clubInformation, requester, clubCategory.getName());
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
    public void decideClubAdmin(String clubName, DecideAdminClubAdminRequest request) {
        ClubCreateRedis createRequest = clubCreateRedisRepository.findById(clubName)
            .orElseThrow(() -> ClubNotFoundException.withDetail("신청한 동아리를 찾을 수 없습니다."));

        if (request.isAccept()) {
            createApprovedClub(createRequest);
        }

        clubCreateRedisRepository.deleteById(clubName);
    }

    public AdminClubAdminsResponse getClubAdmins(ClubAdminCondition condition) {
        int totalCount = clubAdminRepository.countAll();
        Criteria criteria = Criteria.of(condition.page(), condition.limit(), totalCount);
        Sort.Direction direction = condition.getDirection();

        Page<ClubAdmin> result = getClubAdminsResultPage(criteria, direction);

        return AdminClubAdminsResponse.of(result, criteria);
    }

    public AdminClubAdminsResponse getUnacceptedClubAdmins(ClubAdminCondition condition) {
        List<ClubCreateRedis> unAcceptedClubList = (List<ClubCreateRedis>)clubCreateRedisRepository.findAll();
        int totalCount = unAcceptedClubList.size();
        Criteria criteria = Criteria.of(condition.page(), condition.limit(), totalCount);

        Comparator<ClubCreateRedis> comparator =
            Comparator.comparing(ClubCreateRedis::getCreatedAt);
        if (condition.getDirection() == Sort.Direction.DESC) {
            comparator = comparator.reversed();
        }

        List<ClubCreateRedis> sorted = unAcceptedClubList.stream()
            .sorted(comparator)
            .toList();

        List<ClubCreateRedis> paged = sorted.stream()
            .skip((long)criteria.getPage() * criteria.getLimit())
            .limit(criteria.getLimit())
            .toList();

        Map<Integer, User> userMap = userRepository.findAllByIdIn(
            paged.stream().map(ClubCreateRedis::getRequesterId).toList()
        ).stream().collect(Collectors.toMap(User::getId, Function.identity()));

        List<AdminClubAdminsResponse.InnerClubAdminsResponse> responseList = paged.stream()
            .map(redis -> AdminClubAdminsResponse.InnerClubAdminsResponse.fromRedis(
                redis, userMap.get(redis.getRequesterId()))).toList();

        return new AdminClubAdminsResponse(
            (long)totalCount,
            responseList.size(),
            (totalCount + criteria.getLimit() - 1) / criteria.getLimit(),
            criteria.getPage() + 1,
            responseList
        );
    }

    private Page<ClubAdmin> getClubAdminsResultPage(Criteria criteria, Sort.Direction direction) {
        PageRequest pageRequest = PageRequest.of(
            criteria.getPage(),
            criteria.getLimit(),
            Sort.by(direction, "club.createdAt")
        );

        return clubAdminRepository.findPageAll(pageRequest);
    }

    private void createApprovedClub(ClubCreateRedis clubCreateRedis) {
        User requester = userRepository.getById(clubCreateRedis.getRequesterId());
        ClubCategory category = clubCategoryRepository.getById(clubCreateRedis.getClubCategoryId());

        Club club = clubCreateRedis.toClub(category);
        clubRepository.save(club);

        ClubAdmin clubAdmin = clubCreateRedis.toClubAdmin(club, requester);
        clubAdminRepository.save(clubAdmin);
    }
}
