package in.koreatech.koin.domain.teamrecruitment.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.common.model.Criteria;
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
        int total = (int) notificationRepository.countByRecipient_IdAndIsDeletedFalse(userId);
        Criteria criteria = Criteria.of(page, limit, total);

        Page<TeamRecruitmentNotification> result = notificationRepository
                .findAllByRecipient_IdAndIsDeletedFalseOrderByIdDesc(userId, PageRequest.of(criteria.getPage(), criteria.getLimit()));

        long unreadCount = notificationRepository.countByRecipient_IdAndReadAtIsNullAndIsDeletedFalse(userId);

        List<TeamRecruitmentNotificationResponse> notifications = result.getContent().stream()
                .map(TeamRecruitmentNotificationResponse::from)
                .toList();

        return new TeamRecruitmentNotificationsResponse(
                notifications,
                unreadCount,
                total,
                result.getNumberOfElements(),
                result.getTotalPages(),
                criteria.getPage() + 1
        );
    }

    @Transactional
    public void markAsRead(Integer userId, Integer notificationId) {
        notificationRepository.updateReadAtByRecipientIdAndNotificationId(userId, notificationId, LocalDateTime.now());
    }

    @Transactional
    public void delete(Integer userId, Integer notificationId) {
        notificationRepository.updateIsDeletedByRecipientIdAndNotificationId(userId, notificationId);
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
