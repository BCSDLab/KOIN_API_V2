package in.koreatech.koin.domain.notification.eventlistener;

import static in.koreatech.koin._common.model.MobileAppPath.CLUB;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin._common.event.ClubRecruitmentChangeEvent;
import in.koreatech.koin.domain.club.model.ClubRecruitmentSubscription;
import in.koreatech.koin.domain.club.repository.ClubRecruitmentSubscriptionRepository;
import in.koreatech.koin.domain.notification.model.Notification;
import in.koreatech.koin.domain.notification.model.NotificationFactory;
import in.koreatech.koin.domain.notification.service.NotificationService;
import in.koreatech.koin.domain.user.model.User;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class ClubRecruitmentEventListener {

    private final ClubRecruitmentSubscriptionRepository subscriptionRepository;
    private final NotificationFactory notificationFactory;
    private final NotificationService notificationService;

    @TransactionalEventListener
    public void onClubRecruitmentChange(ClubRecruitmentChangeEvent event) {
        List<ClubRecruitmentSubscription> subscriptions = subscriptionRepository.findAllWithUserByClubId(event.clubId());
        if (subscriptions.isEmpty()) return;

        List<Notification> notifications = subscriptions.stream()
            .map(sub -> createClubRecruitmentNotification(event.clubId(), event.clubName(), sub.getUser()))
            .toList();
        notificationService.pushNotifications(notifications);
    }

    private Notification createClubRecruitmentNotification(Integer clubId, String clubName, User user) {
        return notificationFactory.generateClubRecruitmentNotification(
            CLUB,
            clubId,
            clubName,
            user
        );
    }
}
