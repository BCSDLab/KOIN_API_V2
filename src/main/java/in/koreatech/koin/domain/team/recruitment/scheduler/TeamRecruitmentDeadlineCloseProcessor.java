package in.koreatech.koin.domain.team.recruitment.scheduler;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.ACCEPTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.PENDING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomStatus.ACTIVE;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType.TEAM;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationTargetType.CHAT_ROOM;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationTargetType.MY_APPLICATIONS;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType.APPLICATION_REJECTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType.RECRUITMENT_CLOSED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.RECRUITING;
import static in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom.TEAM_ROOM_SCOPE_KEY;

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
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TeamRecruitmentDeadlineCloseProcessor {

    private static final String RECRUITMENT_CLOSED_REASON = "RECRUITMENT_CLOSED";
    private static final String OUTBOX_EVENT_TYPE = "TEAM_RECRUITMENT_NOTIFICATION";
    private static final String AGGREGATE_TYPE = "TEAM_RECRUITMENT";
    private final TeamRecruitmentRepository recruitmentRepository;
    private final TeamRecruitmentApplicationRepository applicationRepository;
    private final TeamRecruitmentChatRoomRepository chatRoomRepository;
    private final TeamRecruitmentNotificationRepository notificationRepository;
    private final TeamRecruitmentOutboxEventRepository outboxEventRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void closeIfExpired(Integer recruitmentId, LocalDate today) {
        Optional<TeamRecruitment> lockedRecruitment = recruitmentRepository.findByIdWithLock(recruitmentId);
        if (lockedRecruitment.isEmpty()) {
            return;
        }

        TeamRecruitment recruitment = lockedRecruitment.get();
        if (recruitment.getStatus() != RECRUITING
            || recruitment.getDeadlineDate() == null
            || !today.isAfter(recruitment.getDeadlineDate())) {
            return;
        }

        List<TeamRecruitmentApplication> pendingApplications = findApplications(recruitmentId, PENDING);
        List<TeamRecruitmentApplication> acceptedApplications = findApplications(recruitmentId, ACCEPTED);
        TeamRecruitmentChatRoom teamRoom = findTeamRoom(recruitmentId);

        recruitment.close();
        recruitmentRepository.save(recruitment);
        rejectPendingApplications(recruitment, pendingApplications);
        markRoomsReadOnly(recruitmentId);
        notifyRejectedApplications(recruitment, pendingApplications);
        notifyAcceptedMembers(recruitment, teamRoom, acceptedApplications);
    }

    private List<TeamRecruitmentApplication> findApplications(
        Integer recruitmentId,
        TeamRecruitmentApplicationStatus status
    ) {
        Page<TeamRecruitmentApplication> applicationPage = applicationRepository.findAllByRecruitment_IdAndStatusIn(
            recruitmentId,
            List.of(status),
            Pageable.unpaged()
        );
        return applicationPage == null ? List.of() : applicationPage.getContent();
    }

    private TeamRecruitmentChatRoom findTeamRoom(Integer recruitmentId) {
        Optional<TeamRecruitmentChatRoom> teamRoom = chatRoomRepository
            .findByRecruitment_IdAndRoomScopeKey(recruitmentId, TEAM_ROOM_SCOPE_KEY);
        if (teamRoom == null) {
            return null;
        }
        return teamRoom.filter(chatRoom -> chatRoom.getRoomType() == TEAM).orElse(null);
    }

    private void rejectPendingApplications(
        TeamRecruitment recruitment,
        List<TeamRecruitmentApplication> pendingApplications
    ) {
        for (TeamRecruitmentApplication application : pendingApplications) {
            application.reject(RECRUITMENT_CLOSED_REASON);
            applicationRepository.save(application);
        }
    }

    private void markRoomsReadOnly(Integer recruitmentId) {
        List<TeamRecruitmentChatRoom> chatRooms = chatRoomRepository.findAllByRecruitment_Id(recruitmentId);
        if (chatRooms == null) {
            return;
        }
        for (TeamRecruitmentChatRoom chatRoom : chatRooms) {
            if (chatRoom.getStatus() == ACTIVE) {
                chatRoom.markReadOnly();
                chatRoomRepository.save(chatRoom);
            }
        }
    }

    private void notifyRejectedApplications(
        TeamRecruitment recruitment,
        List<TeamRecruitmentApplication> pendingApplications
    ) {
        for (TeamRecruitmentApplication application : pendingApplications) {
            TeamRecruitmentNotification notification = TeamRecruitmentNotification.builder()
                .recipient(application.getApplicant())
                .type(APPLICATION_REJECTED)
                .targetType(MY_APPLICATIONS)
                .messagePreview(rejectedMessage(recruitment))
                .recruitment(recruitment)
                .application(application)
                .build();

            saveNotificationAndOutbox(
                notification,
                notificationEventKey(application.getId(), APPLICATION_REJECTED),
                application.getId(),
                null
            );
        }
    }

    private void notifyAcceptedMembers(
        TeamRecruitment recruitment,
        TeamRecruitmentChatRoom teamRoom,
        List<TeamRecruitmentApplication> acceptedApplications
    ) {
        if (teamRoom == null) {
            if (!acceptedApplications.isEmpty()) {
                throw new IllegalStateException(
                    "승인된 지원자가 존재하지만 팀 모집 TEAM 채팅방이 없습니다. recruitmentId: "
                        + recruitment.getId()
                );
            }
            return;
        }

        for (TeamRecruitmentApplication application : acceptedApplications) {
            TeamRecruitmentNotification notification = TeamRecruitmentNotification.builder()
                .recipient(application.getApplicant())
                .type(RECRUITMENT_CLOSED)
                .targetType(CHAT_ROOM)
                .messagePreview(closedMessage(recruitment))
                .recruitment(recruitment)
                .application(application)
                .chatRoom(teamRoom)
                .build();

            saveNotificationAndOutbox(
                notification,
                notificationEventKey(application.getId(), RECRUITMENT_CLOSED),
                application.getId(),
                teamRoom.getId()
            );
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

        TeamRecruitmentNotification savedNotification = notificationRepository.save(notification);
        if (savedNotification == null) {
            savedNotification = notification;
        }
        outboxEventRepository.save(TeamRecruitmentOutboxEvent.builder()
            .eventKey(eventKey)
            .eventType(OUTBOX_EVENT_TYPE)
            .aggregateType(AGGREGATE_TYPE)
            .aggregateId(notification.getRecruitment().getId())
            .payload(payload(savedNotification, applicationId, chatRoomId))
            .status(TeamRecruitmentOutboxEventStatus.PENDING)
            .build());
    }

    private String payload(
        TeamRecruitmentNotification notification,
        Integer applicationId,
        Integer chatRoomId
    ) {
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

    private String notificationEventKey(
        Integer applicationId,
        TeamRecruitmentNotificationType type
    ) {
        return "team-recruitment:application:" + applicationId + ":" + type.name();
    }

    private String jsonNumber(Integer value) {
        return value == null ? "null" : value.toString();
    }

    private String rejectedMessage(TeamRecruitment recruitment) {
        return truncate("지원하신 " + recruitmentTitle(recruitment) + " 모집이 마감되어 지원이 거절되었어요.");
    }

    private String closedMessage(TeamRecruitment recruitment) {
        return truncate(recruitmentTitle(recruitment) + " 모집이 마감되었어요.");
    }

    private String recruitmentTitle(TeamRecruitment recruitment) {
        return recruitment.getTitle() == null ? "팀원 모집" : recruitment.getTitle();
    }

    private String truncate(String message) {
        return message.length() <= 255 ? message : message.substring(0, 255);
    }
}
