package in.koreatech.koin.domain.club.service;

import static in.koreatech.koin._common.code.ApiResponseCode.DUPLICATE_CLUB_RECRUITMENT;
import static in.koreatech.koin.domain.club.enums.ClubSortType.HITS_DESC;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.auth.exception.AuthorizationException;
import in.koreatech.koin._common.event.ClubCreateEvent;
import in.koreatech.koin._common.exception.CustomException;
import in.koreatech.koin.domain.club.dto.request.ClubCreateRequest;
import in.koreatech.koin.domain.club.dto.request.ClubEventCreateRequest;
import in.koreatech.koin.domain.club.dto.request.ClubEventModifyRequest;
import in.koreatech.koin.domain.club.dto.request.ClubIntroductionUpdateRequest;
import in.koreatech.koin.domain.club.dto.request.ClubManagerEmpowermentRequest;
import in.koreatech.koin.domain.club.dto.request.ClubRecruitmentCreateRequest;
import in.koreatech.koin.domain.club.dto.request.ClubRecruitmentModifyRequest;
import in.koreatech.koin.domain.club.dto.request.ClubUpdateRequest;
import in.koreatech.koin.domain.club.dto.request.ClubQnaCreateRequest;
import in.koreatech.koin.domain.club.dto.response.ClubEventResponse;
import in.koreatech.koin.domain.club.dto.response.ClubHotResponse;
import in.koreatech.koin.domain.club.dto.response.ClubHotStatusResponse;
import in.koreatech.koin.domain.club.dto.response.ClubRelatedKeywordResponse;
import in.koreatech.koin.domain.club.dto.response.ClubResponse;
import in.koreatech.koin.domain.club.dto.response.ClubsByCategoryResponse;
import in.koreatech.koin.domain.club.dto.response.ClubQnasResponse;
import in.koreatech.koin.domain.club.enums.ClubEventType;
import in.koreatech.koin.domain.club.enums.ClubSortType;
import in.koreatech.koin.domain.club.enums.SNSType;
import in.koreatech.koin.domain.club.exception.ClubHotNotFoundException;
import in.koreatech.koin.domain.club.exception.ClubLikeDuplicateException;
import in.koreatech.koin.domain.club.exception.ClubLikeNotFoundException;
import in.koreatech.koin.domain.club.exception.ClubManagerAlreadyException;
import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubCategory;
import in.koreatech.koin.domain.club.model.ClubEvent;
import in.koreatech.koin.domain.club.model.ClubHot;
import in.koreatech.koin.domain.club.model.ClubLike;
import in.koreatech.koin.domain.club.model.ClubManager;
import in.koreatech.koin.domain.club.model.ClubQna;
import in.koreatech.koin.domain.club.model.ClubRecruitment;
import in.koreatech.koin.domain.club.model.ClubSNS;
import in.koreatech.koin.domain.club.model.redis.ClubCreateRedis;
import in.koreatech.koin.domain.club.model.redis.ClubHotRedis;
import in.koreatech.koin.domain.club.repository.ClubCategoryRepository;
import in.koreatech.koin.domain.club.repository.ClubEventRepository;
import in.koreatech.koin.domain.club.repository.ClubHotRepository;
import in.koreatech.koin.domain.club.repository.ClubLikeRepository;
import in.koreatech.koin.domain.club.repository.ClubManagerRepository;
import in.koreatech.koin.domain.club.repository.ClubQnaRepository;
import in.koreatech.koin.domain.club.repository.ClubRecruitmentRepository;
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
    private final ClubEventRepository clubEventRepository;
    private final ClubRecruitmentRepository clubRecruitmentRepository;

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

        return ClubResponse.from(club, newSNS, manager, isLiked);
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

        return ClubResponse.from(club, clubSNSs, manager, isLiked);
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
        ClubHotStatusResponse hotStatus = clubHotRepository.findTopByClubIdOrderByIdDesc(clubId)
            .map(this::generateHotStatusResponse)
            .orElse(null);

        return ClubResponse.from(club, clubSNSs, manager, isLiked, hotStatus);
    }

    public ClubsByCategoryResponse getClubByCategory(
        Integer categoryId,
        ClubSortType sortType,
        String query,
        Integer userId
    ) {
        List<Club> clubs = getClubs(categoryId, sortType, query);
        List<Integer> likedClubIds = clubLikeRepository.findClubIdsByUserId(userId);
        return ClubsByCategoryResponse.from(clubs, likedClubIds);
    }

    private List<Club> getClubs(Integer categoryId, ClubSortType sortType, String query) {
        List<Club> clubs;
        if (categoryId == null) {
            clubs = sortType.equals(HITS_DESC)
                ? clubRepository.findByIsActiveTrueOrderByHitsDesc()
                : clubRepository.findByIsActiveTrueOrderByIdAsc();
        } else {
            ClubCategory category = clubCategoryRepository.getById(categoryId);
            clubs = sortType.equals(HITS_DESC)
                ? clubRepository.findByIsActiveTrueAndClubCategoryOrderByHitsDesc(category)
                : clubRepository.findByIsActiveTrueAndClubCategoryOrderByIdAsc(category);
        }

        return query.isEmpty() ? clubs : clubs.stream()
            .filter(queryPredicate(query))
            .toList();
    }

    private Predicate<Club> queryPredicate(String query) {
        String normalizedQuery = normalizeString(query);
        return club -> normalizeString(club.getName()).contains(normalizedQuery);
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
    }

    @Transactional
    public void deleteRecruitment(Integer clubId, Integer studentId) {
        Club club = clubRepository.getById(clubId);
        ClubRecruitment clubRecruitment = clubRecruitmentRepository.getByClub(club);
        Student student = studentRepository.getById(studentId);
        isClubManager(club.getId(), student.getId());

        clubRecruitmentRepository.delete(clubRecruitment);
    }

    @Transactional
    public void createClubEvent(ClubEventCreateRequest request, Integer clubId, Integer studentId) {
        Club club = clubRepository.getById(clubId);
        Student student = studentRepository.getById(studentId);
        isClubManager(club.getId(), student.getId());

        clubEventRepository.save(request.toEntity(club));
    }

    @Transactional
    public void modifyClubEvent(ClubEventModifyRequest request, Integer eventId, Integer clubId, Integer studentId) {
        Club club = clubRepository.getById(clubId);
        ClubEvent clubEvent = clubEventRepository.getClubEventById(eventId);
        Student student = studentRepository.getById(studentId);
        isClubManager(club.getId(), student.getId());

        clubEvent.modifyClubEvent(
            request.name(),
            request.imageUrl(),
            request.startDate(),
            request.endDate(),
            request.introduce(),
            request.content()
        );
    }

    @Transactional
    public void deleteClubEvent(Integer clubId, Integer eventId, Integer studentId) {
        Club club = clubRepository.getById(clubId);
        ClubEvent clubEvent = clubEventRepository.getClubEventById(eventId);
        Student student = studentRepository.getById(studentId);
        isClubManager(club.getId(), student.getId());

        clubEventRepository.delete(clubEvent);
    }

    public ClubEventResponse getClubEvent(Integer clubId, Integer eventId) {
        ClubEvent clubEvent = clubEventRepository.getClubEventByIdAndClubId(eventId, clubId);
        return ClubEventResponse.from(clubEvent, LocalDateTime.now());
    }

    public List<ClubEventResponse> getClubEvents(Integer clubId, ClubEventType eventType) {
        List<ClubEvent> events = clubEventRepository.getAllByClubId(clubId);
        LocalDateTime now = LocalDateTime.now();

        return events.stream()
            .filter(event -> filterEventType(event, eventType, now))
            .sorted(Comparator.comparing(
                event -> ClubEventResponse.calculateStatus(event.getStartDate(), event.getEndDate(), now)
                    .getPriority()))
            .map(event -> ClubEventResponse.from(event, now))
            .toList();
    }

    private boolean filterEventType(ClubEvent event, ClubEventType eventType, LocalDateTime now) {
        if (eventType == null) {
            return true;
        }

        ClubEventType currentType = ClubEventResponse.calculateStatus(
            event.getStartDate(),
            event.getEndDate(),
            now
        );

        return currentType == eventType;
    }

    private ClubHotStatusResponse generateHotStatusResponse(ClubHot hot) {
        List<ClubHot> hotClubs = clubHotRepository.findAllByOrderByIdDesc();
        int count = 0;
        for (ClubHot hotClub : hotClubs) {
            if (!hot.getClub().getId().equals(hotClub.getClub().getId())) break;
            count++;
        }
        return ClubHotStatusResponse.from(hot, count);
    }
}
