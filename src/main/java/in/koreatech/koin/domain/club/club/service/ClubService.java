package in.koreatech.koin.domain.club.club.service;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.common.event.ClubCreateEvent;
import in.koreatech.koin.domain.club.category.model.ClubCategory;
import in.koreatech.koin.domain.club.category.repository.ClubCategoryRepository;
import in.koreatech.koin.domain.club.club.dto.request.ClubCreateRequest;
import in.koreatech.koin.domain.club.club.dto.request.ClubIntroductionUpdateRequest;
import in.koreatech.koin.domain.club.club.dto.request.ClubManagerEmpowermentRequest;
import in.koreatech.koin.domain.club.club.dto.request.ClubUpdateRequest;
import in.koreatech.koin.domain.club.club.dto.response.ClubHotResponse;
import in.koreatech.koin.domain.club.club.dto.response.ClubHotStatusResponse;
import in.koreatech.koin.domain.club.club.dto.response.ClubRelatedKeywordResponse;
import in.koreatech.koin.domain.club.club.dto.response.ClubResponse;
import in.koreatech.koin.domain.club.club.dto.response.ClubsByCategoryResponse;
import in.koreatech.koin.domain.club.club.enums.ClubSortType;
import in.koreatech.koin.domain.club.club.enums.SNSType;
import in.koreatech.koin.domain.club.club.exception.ClubHotNotFoundException;
import in.koreatech.koin.domain.club.club.exception.ClubManagerAlreadyException;
import in.koreatech.koin.domain.club.club.model.Club;
import in.koreatech.koin.domain.club.club.model.ClubBaseInfo;
import in.koreatech.koin.domain.club.club.model.ClubHot;
import in.koreatech.koin.domain.club.club.model.ClubManager;
import in.koreatech.koin.domain.club.club.model.ClubSNS;
import in.koreatech.koin.domain.club.club.model.redis.ClubCreateRedis;
import in.koreatech.koin.domain.club.club.model.redis.ClubHotRedis;
import in.koreatech.koin.domain.club.club.repository.ClubHotRepository;
import in.koreatech.koin.domain.club.club.repository.ClubListQueryRepository;
import in.koreatech.koin.domain.club.club.repository.ClubManagerRepository;
import in.koreatech.koin.domain.club.club.repository.ClubRepository;
import in.koreatech.koin.domain.club.club.repository.ClubSNSRepository;
import in.koreatech.koin.domain.club.club.repository.redis.ClubCreateRedisRepository;
import in.koreatech.koin.domain.club.club.repository.redis.ClubHitsRedisRepository;
import in.koreatech.koin.domain.club.club.repository.redis.ClubHotRedisRepository;
import in.koreatech.koin.domain.club.like.repository.ClubLikeRepository;
import in.koreatech.koin.domain.club.recruitment.repository.ClubRecruitmentSubscriptionRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubService {

    private final ClubHotRedisRepository hotClubRedisRepository;
    private final ClubHotRepository clubHotRepository;
    private final ClubRepository clubRepository;
    private final ClubManagerRepository clubManagerRepository;
    private final ClubCategoryRepository clubCategoryRepository;
    private final ClubSNSRepository clubSNSRepository;
    private final ClubLikeRepository clubLikeRepository;
    private final UserRepository userRepository;
    private final ClubCreateRedisRepository clubCreateRedisRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ClubHitsRedisRepository clubHitsRedisRepository;
    private final ClubListQueryRepository clubListQueryRepository;
    private final ClubRecruitmentSubscriptionRepository clubRecruitmentSubscriptionRepository;

    private static final int RELATED_LIMIT_SIZE = 5;

    @Transactional
    public void createClubRequest(ClubCreateRequest request, Integer studentId) {
        ClubCreateRedis createRedis = ClubCreateRedis.of(request, studentId);
        clubCreateRedisRepository.save(createRedis);

        eventPublisher.publishEvent(new ClubCreateEvent(request.name()));
    }

    @Transactional
    public ClubResponse updateClub(Integer clubId, ClubUpdateRequest request, Integer studentId) {
        Club club = clubRepository.getById(clubId);
        isClubManager(clubId, studentId);

        ClubCategory clubCategory = clubCategoryRepository.getById(request.clubCategoryId());
        club.update(request.name(), request.imageUrl(), clubCategory, request.location(), request.description(),
            request.isLikeHidden());

        List<ClubSNS> newSNS = updateClubSNS(request, club);
        Boolean manager = clubManagerRepository.existsByClubIdAndUserId(clubId, studentId);
        Boolean isLiked = clubLikeRepository.existsByClubIdAndUserId(clubId, studentId);
        Boolean isRecruitSubscribed = clubRecruitmentSubscriptionRepository.existsByClubIdAndUserId(clubId, studentId);

        return ClubResponse.from(club, newSNS, manager, isLiked, isRecruitSubscribed);
    }

    private List<ClubSNS> updateClubSNS(ClubUpdateRequest request, Club club) {
        clubSNSRepository.deleteAllByClub(club);
        List<ClubSNS> newSNS = List.of(
                new ClubSNS(club, SNSType.INSTAGRAM, request.instagram()),
                new ClubSNS(club, SNSType.GOOGLE_FORM, request.googleForm()),
                new ClubSNS(club, SNSType.OPEN_CHAT, request.openChat()),
                new ClubSNS(club, SNSType.PHONE_NUMBER, request.phoneNumber())
            ).stream()
            .filter(sns -> sns.getContact() != null && !sns.getContact().isBlank())
            .toList();

        for (ClubSNS sns : newSNS) {
            clubSNSRepository.save(sns);
        }
        return newSNS;
    }

    @Transactional
    public ClubResponse updateClubIntroduction(
        Integer clubId, ClubIntroductionUpdateRequest request, Integer studentId
    ) {
        Club club = clubRepository.getById(clubId);
        isClubManager(clubId, studentId);

        club.updateIntroduction(request.introduction());
        List<ClubSNS> clubSNSs = club.getClubSNSs();
        Boolean manager = clubManagerRepository.existsByClubIdAndUserId(clubId, studentId);
        Boolean isLiked = clubLikeRepository.existsByClubIdAndUserId(clubId, studentId);
        Boolean isRecruitSubscribed = clubRecruitmentSubscriptionRepository.existsByClubIdAndUserId(clubId, studentId);

        return ClubResponse.from(club, clubSNSs, manager, isLiked, isRecruitSubscribed);
    }

    private void isClubManager(Integer clubId, Integer studentId) {
        if (!clubManagerRepository.existsByClubIdAndUserId(clubId, studentId)) {
            throw AuthorizationException.withDetail("studentId: " + studentId);
        }
    }

    @Transactional
    public ClubResponse getClub(Integer clubId, Integer userId) {
        Club club = clubRepository.getById(clubId);
        if (!club.getIsActive()) {
            throw new IllegalStateException("비활성화 동아리입니다.");
        }
        clubHitsRedisRepository.incrementHits(clubId);

        List<ClubSNS> clubSNSs = clubSNSRepository.findAllByClub(club);
        Boolean manager = clubManagerRepository.existsByClubIdAndUserId(clubId, userId);
        Boolean isLiked = clubLikeRepository.existsByClubIdAndUserId(clubId, userId);
        Boolean isRecruitSubscribed = clubRecruitmentSubscriptionRepository.existsByClubIdAndUserId(clubId, userId);
        ClubHotStatusResponse hotStatus = clubHotRepository.findTopByClubIdOrderByIdDesc(clubId)
            .map(this::generateHotStatusResponse)
            .orElse(null);

        return ClubResponse.from(club, clubSNSs, manager, isLiked, isRecruitSubscribed, hotStatus);
    }

    public ClubsByCategoryResponse getClubByCategory(
        Integer categoryId,
        Boolean isRecruiting,
        ClubSortType sortType,
        String query,
        Integer userId
    ) {
        sortType.validateRecruitingCondition(isRecruiting);
        List<ClubBaseInfo> clubBaseInfos = clubListQueryRepository.findAllClubInfo(categoryId, sortType,
            isRecruiting, query, userId);
        return ClubsByCategoryResponse.from(clubBaseInfos);
    }

    public ClubRelatedKeywordResponse getRelatedClubs(String query) {
        String normalizedQuery = normalizeString(query);
        if (normalizedQuery.isEmpty()) {
            return new ClubRelatedKeywordResponse(List.of());
        }
        PageRequest pageRequest = PageRequest.of(0, RELATED_LIMIT_SIZE);
        List<Club> clubs = clubRepository.findByNamePrefix(normalizedQuery, pageRequest);
        return ClubRelatedKeywordResponse.from(clubs);
    }

    private String normalizeString(String s) {
        return s.replaceAll("\\s+", "").toLowerCase();
    }

    public ClubHotResponse getHotClub() {
        return hotClubRedisRepository.findById(ClubHotRedis.REDIS_KEY)
            .map(ClubHotResponse::from)
            .orElseGet(this::getHotClubFromDBAndCache);
    }

    private ClubHotResponse getHotClubFromDBAndCache() {
        return clubHotRepository.findTopByOrderByEndDateDesc()
            .map(clubHot -> {
                hotClubRedisRepository.save(ClubHotRedis.from(clubHot.getClub()));
                return ClubHotResponse.from(clubHot.getClub());
            })
            .orElseThrow(() -> ClubHotNotFoundException.withDetail(""));
    }

    @Transactional
    public void empowermentClubManager(ClubManagerEmpowermentRequest request, Integer studentId) {
        Club club = clubRepository.getById(request.clubId());
        User currentManager = userRepository.getById(studentId);
        User changedManager = userRepository.getByLoginIdAndUserTypeIn(request.changedManagerId(),
            UserType.KOIN_STUDENT_TYPES);

        isClubManager(request.clubId(), studentId);
        if (clubManagerRepository.existsByClubAndUser(club, changedManager)) {
            throw ClubManagerAlreadyException.withDetail("");
        }
        clubManagerRepository.deleteByClubAndUser(club, currentManager);

        ClubManager newClubManager = ClubManager.builder()
            .club(club)
            .user(changedManager)
            .build();

        clubManagerRepository.save(newClubManager);
    }

    private ClubHotStatusResponse generateHotStatusResponse(ClubHot hot) {
        List<ClubHot> hotClubs = clubHotRepository.findAllByOrderByIdDesc();
        int count = 0;
        for (ClubHot hotClub : hotClubs) {
            if (!hot.getClub().getId().equals(hotClub.getClub().getId()))
                break;
            count++;
        }
        return ClubHotStatusResponse.from(hot, count);
    }
}
