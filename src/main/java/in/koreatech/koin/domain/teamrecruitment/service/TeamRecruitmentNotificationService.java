package in.koreatech.koin.domain.teamrecruitment.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentNotification;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentNotificationRepository;
import in.koreatech.koin.domain.teamrecruitment.dto.TeamRecruitmentNotificationResponse;
import in.koreatech.koin.domain.teamrecruitment.dto.TeamRecruitmentNotificationsResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamRecruitmentNotificationService {

    private final TeamRecruitmentNotificationRepository notificationRepository;

    public TeamRecruitmentNotificationsResponse getNotifications(Integer userId, int page, int limit) {
        int clampedPage = Math.max(1, page);
        int clampedLimit = Math.min(50, Math.max(1, limit));

        Page<TeamRecruitmentNotification> result = notificationRepository
                .findAllByRecipient_IdAndIsDeletedFalseOrderByIdDesc(userId, PageRequest.of(clampedPage - 1, clampedLimit));

        int actualPage = Math.min(clampedPage, Math.max(1, result.getTotalPages()));
        long unreadCount = notificationRepository.countByRecipient_IdAndReadAtIsNullAndIsDeletedFalse(userId);

        List<TeamRecruitmentNotificationResponse> notifications = result.getContent().stream()
                .map(TeamRecruitmentNotificationResponse::from)
                .toList();

        return new TeamRecruitmentNotificationsResponse(
                notifications,
                unreadCount,
                result.getTotalElements(),
                result.getNumberOfElements(),
                result.getTotalPages(),
                actualPage
        );
    }

    @Transactional
    public void markAsRead(Integer userId, Integer notificationId) {
        TeamRecruitmentNotification notification = notificationRepository
                .findByIdAndRecipient_Id(notificationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."));
        notification.markAsRead(LocalDateTime.now());
    }

    @Transactional
    public void markAllRead(Integer userId) {
        LocalDateTime now = LocalDateTime.now();
        List<TeamRecruitmentNotification> notifications =
                notificationRepository.findAllByRecipient_IdAndIsDeletedFalse(userId);
        notifications.forEach(n -> n.markAsRead(now));
    }

    @Transactional
    public void deleteAll(Integer userId) {
        List<TeamRecruitmentNotification> notifications =
                notificationRepository.findAllByRecipient_IdAndIsDeletedFalse(userId);
        notifications.forEach(TeamRecruitmentNotification::delete);
    }
}
