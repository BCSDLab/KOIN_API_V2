package in.koreatech.koin.domain.club.service;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.auth.exception.AuthorizationException;
import in.koreatech.koin.domain.club.dto.request.CreateQnaRequest;
import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubQna;
import in.koreatech.koin.domain.club.repository.ClubAdminRepository;
import in.koreatech.koin.domain.club.repository.ClubQnaRepository;
import in.koreatech.koin.domain.club.repository.ClubRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubService {

    private final ClubQnaRepository clubQnaRepository;
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;
    private final ClubAdminRepository clubAdminRepository;

    @Transactional
    public void createQna(CreateQnaRequest request, Integer clubId, Integer studentId) {
        Club club = clubRepository.getById(clubId);
        User user = userRepository.getById(studentId);
        ClubQna parentQna = request.parentId() == null ? null : clubQnaRepository.getById(request.parentId());
        ClubQna qna = request.toClubQna(club, user, parentQna) ;
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
        if (Objects.equals(qna.getUser().getId(), studentId)) return;
        if (clubAdminRepository.existsByClubIdAndUserId(clubId, studentId)) return;
        throw AuthorizationException.withDetail("studentId: " + studentId);
    }
}
