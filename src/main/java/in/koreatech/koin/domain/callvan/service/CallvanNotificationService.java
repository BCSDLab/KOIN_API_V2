package in.koreatech.koin.domain.callvan.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.callvan.dto.CallvanNotificationResponse;
import in.koreatech.koin.domain.callvan.model.CallvanNotification;
import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.domain.callvan.model.CallvanReport;
import in.koreatech.koin.domain.callvan.model.enums.CallvanNotificationType;
import in.koreatech.koin.domain.callvan.repository.CallvanNotificationRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanPostRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CallvanNotificationService {

    private static final String CALLVAN_WARNING_MESSAGE =
        "콜벤팟 이용 과정에서 신고가 접수되어 운영 검토 후 주의 안내가 전달되었습니다. 이후 동일한 문제가 반복될 경우 콜벤 기능 이용이 제한될 수 있습니다.";
    private final CallvanPostRepository callvanPostRepository;
    private final CallvanNotificationRepository callvanNotificationRepository;
    private final UserRepository userRepository;

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

    @Transactional
    public void notifyReportWarning(Integer recipientId, Integer postId) {
        User recipient = userRepository.getById(recipientId);
        CallvanPost post = callvanPostRepository.getById(postId);
        CallvanNotification callvanNotification = buildNotification(
            recipient, CallvanNotificationType.REPORT_WARNING, post, null,
            CALLVAN_WARNING_MESSAGE, null
        );

        callvanNotificationRepository.save(callvanNotification);
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
