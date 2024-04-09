package in.koreatech.koin.domain.user.model;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.domain.owner.repository.OwnerAttachmentRepository;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.user.repository.StudentRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.domain.slack.SlackClient;
import in.koreatech.koin.global.domain.slack.model.SlackNotificationFactory;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class UserEventListener {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final OwnerRepository ownerRepository;
    private final OwnerAttachmentRepository ownerAttachmentRepository;
    private final SlackClient slackClient;
    private final SlackNotificationFactory slackNotificationFactory;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onUserDeleteEvent(UserDeleteEvent event) {
        User user = userRepository.getByEmail(event.email());
        userRepository.delete(user);
        switch (user.getUserType()) {
            case STUDENT:
                studentRepository.deleteByUserId(user.getId());
                break;
            case OWNER:
                ownerRepository.deleteByUserId(user.getId());
                ownerAttachmentRepository.deleteByOwnerId(user.getId());
        }

        var notification = slackNotificationFactory.generateUserDeleteNotification(event.email());
        slackClient.sendMessage(notification);
    }
}
