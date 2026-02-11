package in.koreatech.koin.domain.callvan.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.domain.callvan.event.CallvanNewMessageEvent;
import in.koreatech.koin.domain.callvan.event.CallvanParticipantJoinedEvent;
import in.koreatech.koin.domain.callvan.event.CallvanRecruitmentCompletedEvent;
import in.koreatech.koin.domain.callvan.model.CallvanNotification;
import in.koreatech.koin.domain.callvan.model.CallvanParticipant;
import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.domain.callvan.model.enums.CallvanNotificationType;
import in.koreatech.koin.domain.callvan.repository.CallvanNotificationRepository;
import in.koreatech.koin.domain.user.model.User;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CallvanNotificationEventListener {

    private final CallvanNotificationService callvanNotificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRecruitmentCompleted(CallvanRecruitmentCompletedEvent event) {
        callvanNotificationService.notifyRecruitmentCompleted(event.postId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNewMessage(CallvanNewMessageEvent event) {
        callvanNotificationService.notifyNewMessageReceived(event.postId(), event.sendUserId(), event.senderNickname(),
            event.content());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onParticipantJoined(CallvanParticipantJoinedEvent event) {
        callvanNotificationService.notifyParticipantJoined(event.callvanPostId(), event.joinUserId(),
            event.joinUserNickname());
    }
}
