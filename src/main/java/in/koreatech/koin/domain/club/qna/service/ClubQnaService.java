package in.koreatech.koin.domain.club.qna.service;

import in.koreatech.koin.domain.club.qna.dto.request.ClubQnaCreateRequest;
import in.koreatech.koin.domain.club.qna.dto.response.ClubQnasResponse;
import in.koreatech.koin.domain.club.club.model.Club;
import in.koreatech.koin.domain.club.qna.model.ClubQna;
import in.koreatech.koin.domain.club.manager.repository.ClubManagerRepository;
import in.koreatech.koin.domain.club.qna.repository.ClubQnaRepository;
import in.koreatech.koin.domain.club.club.repository.ClubRepository;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubQnaService {

    private final ClubQnaRepository clubQnaRepository;
    private final ClubRepository clubRepository;
    private final StudentRepository studentRepository;
    private final ClubManagerRepository clubManagerRepository;

    @Transactional
    public ClubQnasResponse getQnas(Integer clubId) {
        List<ClubQna> qnas = clubQnaRepository.findAllByClubId(clubId);
        return ClubQnasResponse.from(qnas);
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
}
