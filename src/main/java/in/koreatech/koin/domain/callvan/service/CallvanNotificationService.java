package in.koreatech.koin.domain.callvan.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.callvan.dto.CallvanNotificationResponse;
import in.koreatech.koin.domain.callvan.model.CallvanNotification;
import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.domain.callvan.model.enums.CallvanNotificationType;
import in.koreatech.koin.domain.callvan.repository.CallvanNotificationRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanPostRepository;
import in.koreatech.koin.domain.user.model.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CallvanNotificationService {

    private final CallvanPostRepository callvanPostRepository;
    private final CallvanNotificationRepository callvanNotificationRepository;

    public List<CallvanNotificationResponse> getNotifications(Integer userId) {
        return callvanNotificationRepository.findAllByRecipientIdOrderByCreatedAtDesc(userId).stream()
            .map(CallvanNotificationResponse::from)
            .toList();
    }

    @Transactional
    public void markAllRead(Integer userId) {
        callvanNotificationRepository.updateIsReadByRecipientId(userId);
    }

    @Transactional
    public void markAsRead(Integer userId, Integer notificationId) {
        callvanNotificationRepository.updateIsReadByRecipientIdAndNotificationId(userId, notificationId);
    }

    @Transactional
    public void deleteAll(Integer userId) {
        callvanNotificationRepository.updateIsDeletedByRecipientId(userId);
    }

    @Transactional
    public void delete(Integer userId, Integer notificationId) {
        callvanNotificationRepository.updateIsDeletedByRecipientIdAndNotificationId(userId, notificationId);
    }

    @Transactional
    public void notifyRecruitmentCompleted(Integer postId) {
        CallvanPost post = callvanPostRepository.getById(postId);

        List<CallvanNotification> notifications = post.getParticipants().stream()
            .filter(p -> !p.getIsDeleted())
            .map(p -> buildNotification(p.getMember(), CallvanNotificationType.RECRUITMENT_COMPLETE, post,
                null, "해당 콜벤팟 인원이 모두 모집되었습니다. 콜벤을 예약하세요", null))
            .toList();

        if (!notifications.isEmpty()) {
            callvanNotificationRepository.saveAll(notifications);
        }
    }

    @Transactional
    public void notifyParticipantJoined(Integer postId, Integer joinUserId, String joinUserNickname) {
        CallvanPost post = callvanPostRepository.getById(postId);

        List<CallvanNotification> notifications = post.getParticipants().stream()
            .filter(p -> !p.getIsDeleted())
            .filter(p -> !p.getMember().getId().equals(joinUserId))
            .map(p -> buildNotification(p.getMember(), CallvanNotificationType.PARTICIPANT_JOINED, post,
                null, null, joinUserNickname))
            .toList();

        if (!notifications.isEmpty()) {
            callvanNotificationRepository.saveAll(notifications);
        }
    }

    @Transactional
    public void notifyNewMessageReceived(Integer postId, Integer senderId, String senderNickname, String messageContent) {
        CallvanPost post = callvanPostRepository.getById(postId);

        List<CallvanNotification> notifications = post.getParticipants().stream()
            .filter(p -> !p.getIsDeleted())
            .filter(p -> !p.getMember().getId().equals(senderId))
            .map(p -> buildNotification(p.getMember(), CallvanNotificationType.NEW_MESSAGE, post,
                senderNickname, messageContent, null))
            .toList();

        if (!notifications.isEmpty()) {
            callvanNotificationRepository.saveAll(notifications);
        }
    }


    private CallvanNotification buildNotification(User recipient, CallvanNotificationType type, CallvanPost post,
        String senderNickname, String messagePreview, String joinedMemberNickname
    ) {
        return CallvanNotification.builder()
            .recipient(recipient)
            .notificationType(type)
            .post(post)
            .departureType(post.getDepartureType())
            .departureCustomName(post.getDepartureCustomName())
            .arrivalType(post.getArrivalType())
            .arrivalCustomName(post.getArrivalCustomName())
            .departureDate(post.getDepartureDate())
            .departureTime(post.getDepartureTime())
            .currentParticipants(post.getCurrentParticipants())
            .maxParticipants(post.getMaxParticipants())
            .senderNickname(senderNickname)
            .messagePreview(messagePreview)
            .chatRoom(post.getChatRoom())
            .joinedMemberNickname(joinedMemberNickname)
            .build();
    }

}
