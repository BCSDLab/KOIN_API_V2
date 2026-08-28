package in.koreatech.koin.domain.teamrecruitment.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.teamrecruitment.dto.TeamRecruitmentNotificationResponse;
import in.koreatech.koin.domain.teamrecruitment.dto.TeamRecruitmentNotificationsResponse;
import in.koreatech.koin.domain.teamrecruitment.repository.TeamRecruitmentNotificationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamRecruitmentNotificationService {

    private final TeamRecruitmentNotificationRepository notificationRepository;

    public TeamRecruitmentNotificationsResponse getNotifications(Integer userId, int page, int limit) {
        Page<TeamRecruitmentNotificationResponse> result = notificationRepository
                .findAllByRecipientIdOrderByCreatedAtDesc(userId, PageRequest.of(page, limit))
                .map(TeamRecruitmentNotificationResponse::from);

        long unreadCount = notificationRepository.countByRecipientIdAndIsReadFalseAndIsDeletedFalse(userId);

        return new TeamRecruitmentNotificationsResponse(
                result.getContent(),
                result.getNumber(),
                result.getTotalPages(),
                unreadCount
        );
    }

    @Transactional
    public void markAsRead(Integer userId, Integer notificationId) {
        notificationRepository.updateIsReadByRecipientIdAndNotificationId(userId, notificationId);
    }

    @Transactional
    public void markAllRead(Integer userId) {
        notificationRepository.updateIsReadByRecipientId(userId);
    }

    @Transactional
    public void deleteAll(Integer userId) {
        notificationRepository.updateIsDeletedByRecipientId(userId);
    }
}
