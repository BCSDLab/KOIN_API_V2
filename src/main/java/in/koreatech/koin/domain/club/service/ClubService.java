package in.koreatech.koin.domain.club.service;

import static in.koreatech.koin.global.code.ApiResponseCode.DUPLICATE_CLUB_RECRUITMENT;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.global.auth.exception.AuthorizationException;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.common.event.ClubCreateEvent;
import in.koreatech.koin.common.event.ClubRecruitmentChangeEvent;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.domain.club.dto.request.ClubCreateRequest;
import in.koreatech.koin.domain.club.dto.request.ClubEventCreateRequest;
import in.koreatech.koin.domain.club.dto.request.ClubEventModifyRequest;
import in.koreatech.koin.domain.club.dto.request.ClubIntroductionUpdateRequest;
import in.koreatech.koin.domain.club.dto.request.ClubManagerEmpowermentRequest;
import in.koreatech.koin.domain.club.dto.request.ClubQnaCreateRequest;
import in.koreatech.koin.domain.club.dto.request.ClubRecruitmentCreateRequest;
import in.koreatech.koin.domain.club.dto.request.ClubRecruitmentModifyRequest;
import in.koreatech.koin.domain.club.dto.request.ClubUpdateRequest;
import in.koreatech.koin.domain.club.dto.response.ClubEventResponse;
import in.koreatech.koin.domain.club.dto.response.ClubEventsResponse;
import in.koreatech.koin.domain.club.dto.response.ClubHotResponse;
import in.koreatech.koin.domain.club.dto.response.ClubHotStatusResponse;
import in.koreatech.koin.domain.club.dto.response.ClubQnasResponse;
import in.koreatech.koin.domain.club.dto.response.ClubRecruitmentResponse;
import in.koreatech.koin.domain.club.dto.response.ClubRelatedKeywordResponse;
import in.koreatech.koin.domain.club.dto.response.ClubResponse;
import in.koreatech.koin.domain.club.dto.response.ClubsByCategoryResponse;
import in.koreatech.koin.domain.club.enums.ClubEventStatus;
import in.koreatech.koin.domain.club.enums.ClubEventType;
import in.koreatech.koin.domain.club.enums.ClubSortType;
import in.koreatech.koin.domain.club.enums.SNSType;
import in.koreatech.koin.domain.club.exception.ClubHotNotFoundException;
import in.koreatech.koin.domain.club.exception.ClubLikeDuplicateException;
import in.koreatech.koin.domain.club.exception.ClubLikeNotFoundException;
import in.koreatech.koin.domain.club.exception.ClubManagerAlreadyException;
import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubBaseInfo;
import in.koreatech.koin.domain.club.model.ClubCategory;
import in.koreatech.koin.domain.club.model.ClubEvent;
import in.koreatech.koin.domain.club.model.ClubEventImage;
import in.koreatech.koin.domain.club.model.ClubEventSubscription;
import in.koreatech.koin.domain.club.model.ClubHot;
import in.koreatech.koin.domain.club.model.ClubLike;
import in.koreatech.koin.domain.club.model.ClubManager;
import in.koreatech.koin.domain.club.model.ClubQna;
import in.koreatech.koin.domain.club.model.ClubRecruitment;
import in.koreatech.koin.domain.club.model.ClubRecruitmentSubscription;
import in.koreatech.koin.domain.club.model.ClubSNS;
import in.koreatech.koin.domain.club.model.redis.ClubCreateRedis;
import in.koreatech.koin.domain.club.model.redis.ClubHotRedis;
import in.koreatech.koin.domain.club.repository.ClubCategoryRepository;
import in.koreatech.koin.domain.club.repository.ClubEventImageRepository;
import in.koreatech.koin.domain.club.repository.ClubEventRepository;
import in.koreatech.koin.domain.club.repository.ClubEventSubscriptionRepository;
import in.koreatech.koin.domain.club.repository.ClubHotRepository;
import in.koreatech.koin.domain.club.repository.ClubLikeRepository;
import in.koreatech.koin.domain.club.repository.ClubListQueryRepository;
import in.koreatech.koin.domain.club.repository.ClubManagerRepository;
import in.koreatech.koin.domain.club.repository.ClubQnaRepository;
import in.koreatech.koin.domain.club.repository.ClubRecruitmentRepository;
import in.koreatech.koin.domain.club.repository.ClubRecruitmentSubscriptionRepository;
import in.koreatech.koin.domain.club.repository.ClubRepository;
import in.koreatech.koin.domain.club.repository.ClubSNSRepository;
import in.koreatech.koin.domain.club.repository.redis.ClubCreateRedisRepository;
import in.koreatech.koin.domain.club.repository.redis.ClubHitsRedisRepository;
import in.koreatech.koin.domain.club.repository.redis.ClubHotRedisRepository;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubService {

    private final ClubHotRedisRepository hotClubRedisRepository;
    private final ClubHotRepository clubHotRepository;
    private final ClubQnaRepository clubQnaRepository;
    private final ClubRepository clubRepository;
    private final StudentRepository studentRepository;
    private final ClubManagerRepository clubManagerRepository;
    private final ClubCategoryRepository clubCategoryRepository;
    private final ClubSNSRepository clubSNSRepository;
    private final ClubLikeRepository clubLikeRepository;
    private final UserRepository userRepository;
    private final ClubCreateRedisRepository clubCreateRedisRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ClubHitsRedisRepository clubHitsRedisRepository;
    private final ClubRecruitmentRepository clubRecruitmentRepository;
    private final ClubListQueryRepository clubListQueryRepository;
    private final ClubRecruitmentSubscriptionRepository clubRecruitmentSubscriptionRepository;
    private final ClubEventRepository clubEventRepository;
    private final ClubEventImageRepository clubEventImageRepository;
    private final ClubEventSubscriptionRepository clubEventSubscriptionRepository;

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

    @Transactional
    public void likeClub(Integer clubId, Integer userId) {
        Club club = clubRepository.getByIdWithPessimisticLock(clubId);
        User user = userRepository.getById(userId);

        boolean alreadyLiked = clubLikeRepository.existsByClubAndUser(club, user);
        if (alreadyLiked) {
            throw ClubLikeDuplicateException.withDetail(clubId);
        }

        ClubLike clubLike = ClubLike.builder()
            .club(club)
            .user(user)
            .build();

        clubLikeRepository.save(clubLike);
        club.increaseLikes();
    }

    @Transactional
    public void likeClubCancel(Integer clubId, Integer userId) {
        Club club = clubRepository.getByIdWithPessimisticLock(clubId);
        User user = userRepository.getById(userId);

        boolean alreadyLiked = clubLikeRepository.existsByClubAndUser(club, user);
        if (!alreadyLiked) {
            throw ClubLikeNotFoundException.withDetail(club.getId());
        }

        clubLikeRepository.deleteByClubAndUser(club, user);
        club.cancelLikes();
    }

    @Transactional
    public ClubQnasResponse getQnas(Integer clubId) {
        List<ClubQna> qnas = clubQnaRepository.findAllByClubId(clubId);
        return ClubQnasResponse.from(qnas);
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
    public void createQna(ClubQnaCreateRequest request, Integer clubId, Integer studentId) {
        Club club = clubRepository.getById(clubId);
        Student student = studentRepository.getById(studentId);
        boolean isManager = clubManagerRepository.existsByClubIdAndUserId(clubId, studentId);
        boolean isQuestion = request.parentId() == null;
        validateQnaCreateAuthorization(studentId, isQuestion, isManager);
        ClubQna parentQna = request.parentId() == null ? null : clubQnaRepository.getById(request.parentId());
        ClubQna qna = request.toClubQna(club, student, parentQna, isManager);
        clubQnaRepository.save(qna);
    }

    private void validateQnaCreateAuthorization(Integer studentId, boolean isQuestion, boolean isManager) {
        if (isQuestion == isManager) {
            throw AuthorizationException.withDetail("studentId: " + studentId);
        }
    }

    @Transactional
    public void deleteQna(Integer clubId, Integer qnaId, Integer studentId) {
        ClubQna qna = clubQnaRepository.getById(qnaId);
        validateQnaDeleteAuthorization(clubId, qna, studentId);
        qna.detachFromParentIfChild();
        clubQnaRepository.delete(qna);
    }

    private void validateQnaDeleteAuthorization(Integer clubId, ClubQna qna, Integer studentId) {
        if (Objects.equals(qna.getAuthor().getId(), studentId))
            return;
        if (clubManagerRepository.existsByClubIdAndUserId(clubId, studentId))
            return;
        throw AuthorizationException.withDetail("studentId: " + studentId);
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

    @Transactional
    public void createRecruitment(ClubRecruitmentCreateRequest request, Integer clubId, Integer studentId) {
        Club club = clubRepository.getById(clubId);
        Student student = studentRepository.getById(studentId);
        isClubManager(club.getId(), student.getId());

        if (clubRecruitmentRepository.findByClub(club).isPresent()) {
            throw CustomException.of(DUPLICATE_CLUB_RECRUITMENT);
        }

        clubRecruitmentRepository.save(request.toEntity(club));
        eventPublisher.publishEvent(new ClubRecruitmentChangeEvent(club.getName(), club.getId()));
    }

    @Transactional
    public void modifyRecruitment(ClubRecruitmentModifyRequest request, Integer clubId, Integer studentId) {
        Club club = clubRepository.getById(clubId);
        ClubRecruitment clubRecruitment = clubRecruitmentRepository.getByClub(club);
        Student student = studentRepository.getById(studentId);
        isClubManager(club.getId(), student.getId());

        clubRecruitment.modifyClubRecruitment(
            request.startDate(),
            request.endDate(),
            request.isAlwaysRecruiting(),
            request.imageUrl(),
            request.content()
        );
        eventPublisher.publishEvent(new ClubRecruitmentChangeEvent(club.getName(), club.getId()));
    }

    @Transactional
    public void deleteRecruitment(Integer clubId, Integer studentId) {
        Club club = clubRepository.getById(clubId);
        ClubRecruitment clubRecruitment = clubRecruitmentRepository.getByClub(club);
        Student student = studentRepository.getById(studentId);
        isClubManager(club.getId(), student.getId());

        clubRecruitmentRepository.delete(clubRecruitment);
    }

    public ClubRecruitmentResponse getRecruitment(Integer clubId, Integer userId) {
        Club club = clubRepository.getById(clubId);
        club.updateIsManager(userId);
        ClubRecruitment clubRecruitment = clubRecruitmentRepository.getByClub(club);

        return ClubRecruitmentResponse.from(clubRecruitment);
    }

    @Transactional
    public void subscribeRecruitmentNotification(Integer clubId, Integer studentId) {
        Club club = clubRepository.getById(clubId);
        User user = userRepository.getById(studentId);

        if (verifyAlreadySubscribedRecruitment(clubId, studentId))
            return;

        ClubRecruitmentSubscription clubRecruitmentSubscription = ClubRecruitmentSubscription.builder()
            .club(club)
            .user(user)
            .build();
        clubRecruitmentSubscriptionRepository.save(clubRecruitmentSubscription);
    }

    @Transactional
    public void rejectRecruitmentNotification(Integer clubId, Integer studentId) {
        Club club = clubRepository.getById(clubId);
        User user = userRepository.getById(studentId);

        if (!verifyAlreadySubscribedRecruitment(clubId, studentId))
            return;

        clubRecruitmentSubscriptionRepository.deleteByClubIdAndUserId(clubId, studentId);
    }

    private boolean verifyAlreadySubscribedRecruitment(Integer clubId, Integer studentId) {
        return clubRecruitmentSubscriptionRepository.existsByClubIdAndUserId(clubId, studentId);
    }

    @Transactional
    public void createClubEvent(ClubEventCreateRequest request, Integer clubId, Integer studentId) {
        Club club = clubRepository.getById(clubId);
        Student student = studentRepository.getById(studentId);
        isClubManager(club.getId(), student.getId());

        ClubEvent clubEvent = request.toEntity(club);
        clubEventRepository.save(clubEvent);

        if (request.imageUrls() != null && !request.imageUrls().isEmpty()) {
            for (String url : request.imageUrls()) {
                ClubEventImage image = ClubEventImage.builder()
                    .clubEvent(clubEvent)
                    .imageUrl(url)
                    .build();
                clubEventImageRepository.save(image);
            }
        }
    }

    @Transactional
    public void modifyClubEvent(ClubEventModifyRequest request, Integer eventId, Integer clubId, Integer studentId) {
        Club club = clubRepository.getById(clubId);
        ClubEvent clubEvent = clubEventRepository.getById(eventId);
        Student student = studentRepository.getById(studentId);
        isClubManager(club.getId(), student.getId());

        clubEvent.modifyClubEvent(
            request.name(),
            request.startDate(),
            request.endDate(),
            request.introduce(),
            request.content()
        );

        clubEventImageRepository.deleteAllByClubEvent(clubEvent);

        if (request.imageUrls() != null && !request.imageUrls().isEmpty()) {
            for (String url : request.imageUrls()) {
                ClubEventImage image = ClubEventImage.builder()
                    .clubEvent(clubEvent)
                    .imageUrl(url)
                    .build();
                clubEventImageRepository.save(image);
            }
        }
    }

    @Transactional
    public void deleteClubEvent(Integer clubId, Integer eventId, Integer studentId) {
        Club club = clubRepository.getById(clubId);
        ClubEvent clubEvent = clubEventRepository.getById(eventId);
        Student student = studentRepository.getById(studentId);
        isClubManager(club.getId(), student.getId());

        clubEventRepository.delete(clubEvent); // 관련 이미지도 자동 삭제됨
    }

    public ClubEventResponse getClubEvent(Integer clubId, Integer eventId) {
        ClubEvent clubEvent = clubEventRepository.getClubEventByIdAndClubId(eventId, clubId);
        return ClubEventResponse.from(clubEvent, LocalDateTime.now());
    }

    public List<ClubEventsResponse> getClubEvents(Integer clubId, ClubEventType eventType, Integer userId) {
        List<ClubEvent> events = clubEventRepository.getAllByClubId(clubId);
        LocalDateTime now = LocalDateTime.now();

        Set<Integer> subscribedEventIds = new HashSet<>();
        if (userId != null) {
            List<Integer> eventIds = events.stream().map(ClubEvent::getId).toList();
            subscribedEventIds.addAll(clubEventSubscriptionRepository.findSubscribedEventIds(userId, eventIds));
        }

        return events.stream()
            .filter(event -> filterEventType(event, eventType, now))
            .sorted((e1, e2) -> compareEvents(e1, e2, eventType, now))
            .map(event -> {
                boolean isSubscribed = subscribedEventIds.contains(event.getId());
                return ClubEventsResponse.from(event, now, isSubscribed);
            })
            .toList();
    }

    private boolean filterEventType(ClubEvent event, ClubEventType eventType, LocalDateTime now) {
        ClubEventStatus status = ClubEventResponse.calculateStatus(event.getStartDate(), event.getEndDate(), now);

        if (eventType == null || eventType == ClubEventType.RECENT)
            return true;

        if (eventType == ClubEventType.ONGOING) {
            return status == ClubEventStatus.ONGOING || status == ClubEventStatus.SOON;
        }

        return status.name().equals(eventType.name());
    }

    private int compareEvents(ClubEvent e1, ClubEvent e2, ClubEventType eventType, LocalDateTime now) {
        ClubEventStatus status1 = ClubEventResponse.calculateStatus(e1.getStartDate(), e1.getEndDate(), now);
        ClubEventStatus status2 = ClubEventResponse.calculateStatus(e2.getStartDate(), e2.getEndDate(), now);

        if (eventType == ClubEventType.RECENT) {
            boolean isEnded1 = status1 == ClubEventStatus.ENDED;
            boolean isEnded2 = status2 == ClubEventStatus.ENDED;

            if (isEnded1 != isEnded2) {
                return Boolean.compare(isEnded1, isEnded2);
            }

            return e2.getCreatedAt().compareTo(e1.getCreatedAt());
        }

        int priority1 = status1.getPriority();
        int priority2 = status2.getPriority();

        if (priority1 != priority2) {
            return Integer.compare(priority1, priority2);
        }

        return e2.getCreatedAt().compareTo(e1.getCreatedAt());
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

    @Transactional
    public void subscribeEventNotification(Integer clubId, Integer eventId, Integer studentId) {
        Club club = clubRepository.getById(clubId);
        User user = userRepository.getById(studentId);
        ClubEvent clubEvent = clubEventRepository.getById(eventId);

        if (!clubEvent.getClub().getId().equals(clubId)) {
            throw CustomException.of(ApiResponseCode.NOT_MATCHED_CLUB_AND_EVENT);
        }

        if (verifyAlreadySubscribed(eventId, studentId))
            return;

        ClubEventSubscription clubEventSubscription = ClubEventSubscription.builder()
            .clubEvent(clubEvent)
            .user(user)
            .build();
        clubEventSubscriptionRepository.save(clubEventSubscription);
    }

    @Transactional
    public void rejectEventNotification(Integer clubId, Integer eventId, Integer studentId) {
        Club club = clubRepository.getById(clubId);
        User user = userRepository.getById(studentId);
        ClubEvent clubEvent = clubEventRepository.getById(eventId);

        if (!clubEvent.getClub().getId().equals(clubId)) {
            throw CustomException.of(ApiResponseCode.NOT_MATCHED_CLUB_AND_EVENT);
        }

        if (!verifyAlreadySubscribed(eventId, studentId))
            return;

        clubEventSubscriptionRepository.deleteByClubEventIdAndUserId(eventId, studentId);
    }

    private boolean verifyAlreadySubscribed(Integer eventId, Integer studentId) {
        return clubEventSubscriptionRepository.existsByClubEventIdAndUserId(eventId, studentId);
    }
}
