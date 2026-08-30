package in.koreatech.koin.domain.team.recruitment.service;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.ACCEPTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.PENDING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomStatus.ACTIVE;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationTargetType.CHAT_ROOM;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationTargetType.MY_APPLICATIONS;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType.APPLICATION_REJECTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType.RECRUITMENT_CLOSED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType.RECRUITMENT_DELETED;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentOutboxEventStatus;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentApplication;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentNotification;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentOutboxEvent;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentApplicationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatRoomRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentNotificationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentOutboxEventRepository;
import lombok.RequiredArgsConstructor;

/**
 * 작성자의 수동 마감과 삭제에 따른 후속 처리.
 * <p>
 * 마감 자동 처리 스케줄러는 RECRUITING 이고 지원 마감일이 지난 모집글만 대상으로 하므로,
 * 작성자가 직접 마감하거나 삭제한 모집글은 스케줄러가 다시 잡지 않는다.
 * 그래서 동일한 후속 처리를 이 컴포넌트가 담당한다.
 * <p>
 * 알림과 Outbox 적재 방식은 TeamRecruitmentDeadlineCloseProcessor 와 같은 규약을 따른다.
 * eventKey 가 같으면 건너뛰므로 스케줄러와 중복 적재되지 않는다.
 */
@Component
@RequiredArgsConstructor
public class TeamRecruitmentClosureService {

    private static final String RECRUITMENT_CLOSED_REASON = "RECRUITMENT_CLOSED";
    private static final String RECRUITMENT_DELETED_REASON = "RECRUITMENT_DELETED";
    private static final String OUTBOX_EVENT_TYPE = "TEAM_RECRUITMENT_NOTIFICATION";
    private static final String AGGREGATE_TYPE = "TEAM_RECRUITMENT";
    private static final int MESSAGE_PREVIEW_MAX_LENGTH = 255;

    private final TeamRecruitmentApplicationRepository applicationRepository;
    private final TeamRecruitmentChatRoomRepository chatRoomRepository;
    private final TeamRecruitmentNotificationRepository notificationRepository;
    private final TeamRecruitmentOutboxEventRepository outboxEventRepository;

    /**
     * 대기 중인 지원서를 거절하고, 모든 채팅방을 읽기 전용으로 바꾸고,
     * 대기 중이던 지원자와 승인된 팀원에게 알림을 남긴다.
     */
    public void onClosed(TeamRecruitment recruitment) {
        List<TeamRecruitmentApplication> pending = findApplications(recruitment.getId(), PENDING);
        List<TeamRecruitmentApplication> accepted = findApplications(recruitment.getId(), ACCEPTED);

        rejectAll(pending, RECRUITMENT_CLOSED_REASON);
        markRoomsReadOnly(recruitment.getId());
        notifyRejected(recruitment, pending, closedRejectedMessage(recruitment));
        notifyAccepted(recruitment, accepted, RECRUITMENT_CLOSED, closedMessage(recruitment));
    }

    /**
     * 삭제도 마감과 같은 후속 처리를 하되 거절 사유와 알림 문구를 삭제 기준으로 남긴다.
     */
    public void onDeleted(TeamRecruitment recruitment) {
        List<TeamRecruitmentApplication> pending = findApplications(recruitment.getId(), PENDING);
        List<TeamRecruitmentApplication> accepted = findApplications(recruitment.getId(), ACCEPTED);

        rejectAll(pending, RECRUITMENT_DELETED_REASON);
        markRoomsReadOnly(recruitment.getId());
        notifyRejected(recruitment, pending, deletedRejectedMessage(recruitment));
        notifyAccepted(recruitment, accepted, RECRUITMENT_DELETED, deletedMessage(recruitment));
    }

    /**
     * 정원이 차서 자동 마감되는 경우. 기한 경과나 수동 마감과 달리 TEAM 채팅방은 ACTIVE 를 유지한다.
     */
    public void onCapacityFull(TeamRecruitment recruitment) {
        List<TeamRecruitmentApplication> pending = findApplications(recruitment.getId(), PENDING);
        List<TeamRecruitmentApplication> accepted = findApplications(recruitment.getId(), ACCEPTED);

        rejectAll(pending, RECRUITMENT_CLOSED_REASON);
        notifyRejected(recruitment, pending, closedRejectedMessage(recruitment));
        notifyAccepted(recruitment, accepted, RECRUITMENT_CLOSED, closedMessage(recruitment));
    }

    private List<TeamRecruitmentApplication> findApplications(
        Integer recruitmentId,
        TeamRecruitmentApplicationStatus status
    ) {
        Page<TeamRecruitmentApplication> page = applicationRepository.findAllByRecruitment_IdAndStatusIn(
            recruitmentId, List.of(status), Pageable.unpaged());
        return page == null ? List.of() : page.getContent();
    }

    private void rejectAll(List<TeamRecruitmentApplication> applications, String reason) {
        applications.forEach(application -> application.reject(reason));
    }

    private void markRoomsReadOnly(Integer recruitmentId) {
        List<TeamRecruitmentChatRoom> chatRooms = chatRoomRepository.findAllByRecruitment_Id(recruitmentId);
        if (chatRooms == null) {
            return;
        }
        chatRooms.stream()
            .filter(chatRoom -> chatRoom.getStatus() == ACTIVE)
            .forEach(TeamRecruitmentChatRoom::markReadOnly);
    }

    private void notifyRejected(
        TeamRecruitment recruitment,
        List<TeamRecruitmentApplication> pending,
        String messagePreview
    ) {
        for (TeamRecruitmentApplication application : pending) {
            TeamRecruitmentNotification notification = TeamRecruitmentNotification.builder()
                .recipient(application.getApplicant())
                .type(APPLICATION_REJECTED)
                .targetType(MY_APPLICATIONS)
                .messagePreview(messagePreview)
                .recruitment(recruitment)
                .application(application)
                .build();
            saveNotificationAndOutbox(notification, eventKey(application.getId(), APPLICATION_REJECTED),
                application.getId(), null);
        }
    }

    private void notifyAccepted(
        TeamRecruitment recruitment,
        List<TeamRecruitmentApplication> accepted,
        TeamRecruitmentNotificationType type,
        String messagePreview
    ) {
        if (accepted.isEmpty()) {
            return;
        }
        TeamRecruitmentChatRoom teamRoom = chatRoomRepository
            .findByRecruitment_IdAndRoomScopeKey(recruitment.getId(), TeamRecruitmentChatRoom.TEAM_ROOM_SCOPE_KEY)
            .orElse(null);
        for (TeamRecruitmentApplication application : accepted) {
            TeamRecruitmentNotification notification = TeamRecruitmentNotification.builder()
                .recipient(application.getApplicant())
                .type(type)
                .targetType(teamRoom == null ? MY_APPLICATIONS : CHAT_ROOM)
                .messagePreview(messagePreview)
                .recruitment(recruitment)
                .application(application)
                .chatRoom(teamRoom)
                .build();
            saveNotificationAndOutbox(notification, eventKey(application.getId(), type),
                application.getId(), teamRoom == null ? null : teamRoom.getId());
        }
    }

    private void saveNotificationAndOutbox(
        TeamRecruitmentNotification notification,
        String eventKey,
        Integer applicationId,
        Integer chatRoomId
    ) {
        if (outboxEventRepository.findByEventKey(eventKey).isPresent()) {
            return;
        }
        TeamRecruitmentNotification saved = notificationRepository.save(notification);
        if (saved == null) {
            saved = notification;
        }
        outboxEventRepository.save(TeamRecruitmentOutboxEvent.builder()
            .eventKey(eventKey)
            .eventType(OUTBOX_EVENT_TYPE)
            .aggregateType(AGGREGATE_TYPE)
            .aggregateId(notification.getRecruitment().getId())
            .payload(payload(saved, applicationId, chatRoomId))
            .status(TeamRecruitmentOutboxEventStatus.PENDING)
            .build());
    }

    private String payload(TeamRecruitmentNotification notification, Integer applicationId, Integer chatRoomId) {
        Integer recipientId = notification.getRecipient() == null ? null : notification.getRecipient().getId();
        return "{"
            + "\"type\":\"" + notification.getType().name() + "\","
            + "\"target_type\":\"" + notification.getTargetType().name() + "\","
            + "\"recipient_id\":" + jsonNumber(recipientId) + ","
            + "\"recruitment_id\":" + jsonNumber(notification.getRecruitment().getId()) + ","
            + "\"application_id\":" + jsonNumber(applicationId) + ","
            + "\"chat_room_id\":" + jsonNumber(chatRoomId) + ","
            + "\"notification_id\":" + jsonNumber(notification.getId())
            + "}";
    }

    private String eventKey(Integer applicationId, TeamRecruitmentNotificationType type) {
        return "team-recruitment:application:" + applicationId + ":" + type.name();
    }

    private String jsonNumber(Integer value) {
        return value == null ? "null" : value.toString();
    }

    private String closedRejectedMessage(TeamRecruitment recruitment) {
        return truncate("지원하신 " + title(recruitment) + " 모집이 마감되어 지원이 거절되었어요.");
    }

    private String deletedRejectedMessage(TeamRecruitment recruitment) {
        return truncate("지원하신 " + title(recruitment) + " 모집이 삭제되어 지원이 취소되었어요.");
    }

    private String closedMessage(TeamRecruitment recruitment) {
        return truncate(title(recruitment) + " 모집이 마감되었어요.");
    }

    private String deletedMessage(TeamRecruitment recruitment) {
        return truncate(title(recruitment) + " 모집이 삭제되었어요.");
    }

    private String title(TeamRecruitment recruitment) {
        return recruitment.getTitle() == null ? "팀원 모집" : recruitment.getTitle();
    }

    private String truncate(String message) {
        return message.length() <= MESSAGE_PREVIEW_MAX_LENGTH
            ? message
            : message.substring(0, MESSAGE_PREVIEW_MAX_LENGTH);
    }
}
