package in.koreatech.koin.domain.club.recruitment.service;

import in.koreatech.koin.common.event.ClubRecruitmentChangeEvent;
import in.koreatech.koin.domain.club.club.model.Club;
import in.koreatech.koin.domain.club.club.repository.ClubRepository;
import in.koreatech.koin.domain.club.manager.service.ClubManagerService;
import in.koreatech.koin.domain.club.recruitment.dto.request.ClubRecruitmentCreateRequest;
import in.koreatech.koin.domain.club.recruitment.dto.request.ClubRecruitmentModifyRequest;
import in.koreatech.koin.domain.club.recruitment.dto.response.ClubRecruitmentResponse;
import in.koreatech.koin.domain.club.recruitment.model.ClubRecruitment;
import in.koreatech.koin.domain.club.recruitment.model.ClubRecruitmentSubscription;
import in.koreatech.koin.domain.club.recruitment.repository.ClubRecruitmentRepository;
import in.koreatech.koin.domain.club.recruitment.repository.ClubRecruitmentSubscriptionRepository;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static in.koreatech.koin.global.code.ApiResponseCode.DUPLICATE_CLUB_RECRUITMENT;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubRecruitmentService {

    private final ClubRepository clubRepository;
    private final StudentRepository studentRepository;
    private final ClubRecruitmentRepository clubRecruitmentRepository;
    private final UserRepository userRepository;
    private final ClubRecruitmentSubscriptionRepository clubRecruitmentSubscriptionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ClubManagerService clubManagerService;

    @Transactional
    public void createRecruitment(ClubRecruitmentCreateRequest request, Integer clubId, Integer studentId) {
        Club club = clubRepository.getById(clubId);
        Student student = studentRepository.getById(studentId);
        clubManagerService.isClubManager(club.getId(), student.getId());

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
        clubManagerService.isClubManager(club.getId(), student.getId());

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
        clubManagerService.isClubManager(club.getId(), student.getId());

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
}
