package in.koreatech.koin.domain.club.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.club.dto.response.ClubHotResponse;
import in.koreatech.koin.domain.club.exception.ClubHotNotFoundException;
import in.koreatech.koin.domain.club.model.redis.ClubHotRedis;
import in.koreatech.koin.domain.club.repository.ClubHotRepository;
import in.koreatech.koin.domain.club.repository.redis.ClubHotRedisRepository;
import in.koreatech.koin._common.auth.exception.AuthorizationException;
import in.koreatech.koin.domain.club.dto.request.CreateQnaRequest;
import in.koreatech.koin.domain.club.dto.response.QnasResponse;
import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubQna;
import in.koreatech.koin.domain.club.repository.ClubAdminRepository;
import in.koreatech.koin.domain.club.repository.ClubQnaRepository;
import in.koreatech.koin.domain.club.repository.ClubRepository;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
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
        if (Objects.equals(qna.getAuthor().getId(), studentId)) return;
        if (clubAdminRepository.existsByClubIdAndUserId(clubId, studentId)) return;
        throw AuthorizationException.withDetail("studentId: " + studentId);
    }
}
