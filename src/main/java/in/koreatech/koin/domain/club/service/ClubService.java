package in.koreatech.koin.domain.club.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.auth.exception.AuthorizationException;
import in.koreatech.koin.admin.club.repository.AdminClubAdminRepository;
import in.koreatech.koin.admin.user.repository.AdminUserRepository;
import in.koreatech.koin.domain.club.dto.request.CreateClubRequest;
import in.koreatech.koin.domain.club.dto.request.CreateQnaRequest;
import in.koreatech.koin.domain.club.dto.response.ClubHotResponse;
import in.koreatech.koin.domain.club.dto.response.ClubResponse;
import in.koreatech.koin.domain.club.dto.response.GetClubByCategoryResponse;
import in.koreatech.koin.domain.club.dto.response.QnasResponse;
import in.koreatech.koin.domain.club.exception.ClubHotNotFoundException;
import in.koreatech.koin.domain.club.exception.ClubLikeNotFoundException;
import in.koreatech.koin.domain.club.exception.DuplicateClubLikiException;
import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubCategory;
import in.koreatech.koin.domain.club.model.ClubLike;
import in.koreatech.koin.domain.club.model.ClubQna;
import in.koreatech.koin.domain.club.model.ClubSNS;
import in.koreatech.koin.domain.club.model.redis.ClubCreateRedis;
import in.koreatech.koin.domain.club.model.redis.ClubHotRedis;
import in.koreatech.koin.domain.club.repository.ClubAdminRepository;
import in.koreatech.koin.domain.club.repository.ClubCategoryRepository;
import in.koreatech.koin.domain.club.repository.ClubHotRepository;
import in.koreatech.koin.domain.club.repository.ClubLikeRepository;
import in.koreatech.koin.domain.club.repository.ClubQnaRepository;
import in.koreatech.koin.domain.club.repository.ClubRepository;
import in.koreatech.koin.domain.club.repository.ClubSNSRepository;
import in.koreatech.koin.domain.club.repository.redis.ClubCreateRedisRepository;
import in.koreatech.koin.domain.club.repository.redis.ClubHotRedisRepository;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.user.model.User;
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
    private final ClubAdminRepository clubAdminRepository;
    private final ClubCategoryRepository clubCategoryRepository;
    private final AdminClubAdminRepository adminClubAdminRepository;
    private final AdminUserRepository adminUserRepository;
    private final ClubSNSRepository clubSNSRepository;
    private final ClubLikeRepository clubLikeRepository;
    private final UserRepository userRepository;
    private final ClubCreateRedisRepository clubCreateRedisRepository;

    @Transactional
    public void createClubRequest(CreateClubRequest request, Integer studentId) {
        ClubCreateRedis createRedis = ClubCreateRedis.of(request, studentId);
        clubCreateRedisRepository.save(createRedis);
    }

    @Transactional
    public ClubResponse getClub(Integer clubId) {
        Club club = clubRepository.getByIdWithPessimisticLock(clubId);
        club.increaseHits();
        List<ClubSNS> clubSNSs = clubSNSRepository.findAllByClub(club);

        return ClubResponse.from(club, clubSNSs);
    }

    public GetClubByCategoryResponse getClubByCategory(Integer categoryId, String sort) {
        ClubCategory category = clubCategoryRepository.getById(categoryId);

        if ("hits".equalsIgnoreCase(sort)) {
            List<Club> clubs = clubRepository.findByClubCategoryOrderByHitsDesc(category);
            return GetClubByCategoryResponse.from(clubs);
        }

        List<Club> clubs = clubRepository.findByClubCategoryOrderByIdAsc(category);
        return GetClubByCategoryResponse.from(clubs);
    }

    @Transactional
    public void likeClub(Integer clubId, Integer userId) {
        Club club = clubRepository.getByIdWithPessimisticLock(clubId);
        User user = userRepository.getById(userId);

        boolean alreadyLiked = clubLikeRepository.existsByClubAndUser(club, user);
        if (alreadyLiked){
            throw DuplicateClubLikiException.withDetail(clubId);
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
        if(!alreadyLiked){
            throw ClubLikeNotFoundException.withDetail(club.getId());
        }

        clubLikeRepository.deleteByClubAndUser(club, user);
        club.cancelLikes();
    }

    @Transactional
    public QnasResponse getQnas(Integer clubId) {
        List<ClubQna> qnas = clubQnaRepository.findAllByClubId(clubId);
        return QnasResponse.from(qnas);
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
    public void createQna(CreateQnaRequest request, Integer clubId, Integer studentId) {
        Club club = clubRepository.getById(clubId);
        Student student = studentRepository.getById(studentId);
        ClubQna parentQna = request.parentId() == null ? null : clubQnaRepository.getById(request.parentId());
        boolean isAdmin = clubAdminRepository.existsByClubIdAndUserId(clubId, studentId);
        ClubQna qna = request.toClubQna(club, student, parentQna, isAdmin);
        clubQnaRepository.save(qna);
    }

    @Transactional
    public void deleteQna(Integer clubId, Integer qnaId, Integer studentId) {
        ClubQna qna = clubQnaRepository.getById(qnaId);
        validateQnaDeleteAuthorization(clubId, qna, studentId);
        if (qna.isRoot()) {
            clubQnaRepository.delete(qna);
        } else {
            qna.delete();
        }
    }

    private void validateQnaDeleteAuthorization(Integer clubId, ClubQna qna, Integer studentId) {
        if (Objects.equals(qna.getAuthor().getId(), studentId))
            return;
        if (clubAdminRepository.existsByClubIdAndUserId(clubId, studentId))
            return;
        throw AuthorizationException.withDetail("studentId: " + studentId);
    }
}
