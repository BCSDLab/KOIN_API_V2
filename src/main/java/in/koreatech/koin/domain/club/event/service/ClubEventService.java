package in.koreatech.koin.domain.club.event.service;

import in.koreatech.koin.domain.club.event.dto.request.ClubEventCreateRequest;
import in.koreatech.koin.domain.club.event.dto.request.ClubEventModifyRequest;
import in.koreatech.koin.domain.club.event.dto.response.ClubEventResponse;
import in.koreatech.koin.domain.club.event.dto.response.ClubEventsResponse;
import in.koreatech.koin.domain.club.event.enums.ClubEventStatus;
import in.koreatech.koin.domain.club.event.enums.ClubEventType;
import in.koreatech.koin.domain.club.event.repository.ClubEventImageRepository;
import in.koreatech.koin.domain.club.event.repository.ClubEventRepository;
import in.koreatech.koin.domain.club.event.repository.ClubEventSubscriptionRepository;
import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.event.model.ClubEvent;
import in.koreatech.koin.domain.club.event.model.ClubEventImage;
import in.koreatech.koin.domain.club.event.model.ClubEventSubscription;
import in.koreatech.koin.domain.club.repository.*;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubEventService {

    private final ClubRepository clubRepository;
    private final StudentRepository studentRepository;
    private final ClubEventRepository clubEventRepository;
    private final ClubEventImageRepository clubEventImageRepository;
    private final ClubEventSubscriptionRepository clubEventSubscriptionRepository;
    private final ClubManagerRepository clubManagerRepository;
    private final UserRepository userRepository;

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

    private void isClubManager(Integer clubId, Integer studentId) {
        if (!clubManagerRepository.existsByClubIdAndUserId(clubId, studentId)) {
            throw AuthorizationException.withDetail("studentId: " + studentId);
        }
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
