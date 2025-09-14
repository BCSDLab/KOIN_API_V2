package in.koreatech.koin.domain.club.manager.service;

import in.koreatech.koin.domain.club.manager.dto.request.ClubManagerEmpowermentRequest;
import in.koreatech.koin.domain.club.manager.exception.ClubManagerAlreadyException;
import in.koreatech.koin.domain.club.club.model.Club;
import in.koreatech.koin.domain.club.manager.model.ClubManager;
import in.koreatech.koin.domain.club.manager.repository.ClubManagerRepository;
import in.koreatech.koin.domain.club.club.repository.ClubRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubManagerService {

    private final ClubRepository clubRepository;
    private final ClubManagerRepository clubManagerRepository;
    private final UserRepository userRepository;


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

    public void isClubManager(Integer clubId, Integer studentId) {
        if (!clubManagerRepository.existsByClubIdAndUserId(clubId, studentId)) {
            throw AuthorizationException.withDetail("studentId: " + studentId);
        }
    }
}
