package in.koreatech.koin.domain.team.recruitment.scheduler;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType.APPLICATION_ACCEPTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType.APPLICATION_REJECTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType.NEW_APPLICATION;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType.NEW_CHAT_MESSAGE;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType.RECRUITMENT_CLOSED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType.RECRUITMENT_DELETED;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationTargetType;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentNotification;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentNotificationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.infrastructure.fcm.FcmClient;
import in.koreatech.koin.infrastructure.fcm.FcmSendRequest;
import in.koreatech.koin.infrastructure.fcm.FcmSendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamRecruitmentOutboxWorker {

    private static final Set<TeamRecruitmentNotificationType> SUPPORTED_TYPES = Set.of(
        NEW_APPLICATION,
        APPLICATION_ACCEPTED,
        APPLICATION_REJECTED,
        RECRUITMENT_CLOSED,
        RECRUITMENT_DELETED,
        NEW_CHAT_MESSAGE
    );
    private static final Set<String> PERMANENT_FCM_CODES = Set.of(
        "INVALID_ARGUMENT",
        "UNREGISTERED",
        "SENDER_ID_MISMATCH",
        "THIRD_PARTY_AUTH_ERROR"
    );

    private final TeamRecruitmentOutboxLeaseService leaseService;
    private final TeamRecruitmentOutboxProperties properties;
    private final TeamRecruitmentNotificationRepository notificationRepository;
    private final TeamRecruitmentRepository recruitmentRepository;
    private final UserRepository userRepository;
    private final FcmClient fcmClient;
    private final ObjectMapper objectMapper;

    public void processBatch() {
        if (!properties.isEnabled()) {
            return;
        }

        String workerId = UUID.randomUUID().toString();
        List<TeamRecruitmentOutboxLeaseService.OutboxClaim> claims;
        try {
            claims = leaseService.claim(workerId, properties.getBoundedBatchSize());
        } catch (Exception exception) {
            log.error("팀원 모집 outbox claim 중 오류가 발생했습니다.", exception);
            return;
        }

        for (TeamRecruitmentOutboxLeaseService.OutboxClaim claim : claims) {
            publish(claim, workerId);
        }
    }

    private void publish(TeamRecruitmentOutboxLeaseService.OutboxClaim claim, String workerId) {
        try {
            JsonNode payload = objectMapper.readTree(claim.payload());
            OutboxPayload outboxPayload = OutboxPayload.from(payload);
            if (!SUPPORTED_TYPES.contains(outboxPayload.type())) {
                fail(claim, workerId, "UNSUPPORTED_EVENT_TYPE", false);
                return;
            }

            Optional<TeamRecruitmentNotification> inbox = findInbox(outboxPayload);
            if (inbox.isPresent() && Boolean.TRUE.equals(inbox.get().getIsDeleted())) {
                complete(claim, workerId);
                return;
            }
            if (outboxPayload.notificationId() != null && inbox.isEmpty()) {
                throw new InvalidPayloadException("NOTIFICATION_NOT_FOUND");
            }
            TeamRecruitment recruitment = inbox
                .map(TeamRecruitmentNotification::getRecruitment)
                .orElseGet(() -> recruitmentRepository.findById(outboxPayload.recruitmentId()).orElse(null));
            if (recruitment == null) {
                throw new InvalidPayloadException("RECRUITMENT_NOT_FOUND");
            }

            User recipient = userRepository.findById(outboxPayload.recipientId()).orElse(null);
            if (recipient == null) {
                throw new InvalidPayloadException("RECIPIENT_NOT_FOUND");
            }
            if (!StringUtils.hasText(recipient.getDeviceToken())) {
                fail(claim, workerId, "NO_DEVICE_TOKEN", false);
                return;
            }

            String recruitmentTitle = recruitment.getTitle() == null
                ? "팀원 모집"
                : recruitment.getTitle();
            String title = text(payload, "title").orElse(recruitmentTitle);
            String content = text(payload, "content")
                .or(() -> text(payload, "message_preview"))
                .or(() -> inbox.map(TeamRecruitmentNotification::getMessagePreview))
                .orElseGet(() -> fallbackContent(outboxPayload.type(), recruitmentTitle));

            FcmSendRequest request = FcmSendRequest.of(
                recipient.getDeviceToken(),
                title,
                content,
                null,
                null,
                null,
                outboxPayload.type().name().toLowerCase(Locale.ROOT),
                outboxPayload.notificationId() == null
                    ? null
                    : outboxPayload.notificationId().toString()
            );
            List<FcmSendResponse> responses = fcmClient.sendMessages(List.of(request));
            FcmSendResponse response = responses == null || responses.isEmpty()
                ? FcmSendResponse.failed("EMPTY_RESPONSE", null)
                : responses.get(0);
            if (response != null && response.success()) {
                complete(claim, workerId);
                return;
            }

            String reason = response == null
                ? "EMPTY_RESPONSE"
                : firstText(response.errorCode(), response.messagingErrorCode(), "FCM_SEND_FAILED");
            fail(claim, workerId, reason, isRetryable(response));
        } catch (JsonProcessingException | InvalidPayloadException exception) {
            fail(claim, workerId, exception.getMessage(), false);
        } catch (Exception exception) {
            fail(claim, workerId, exceptionSummary(exception), true);
        }
    }

    private Optional<TeamRecruitmentNotification> findInbox(OutboxPayload payload) {
        if (payload.notificationId() != null) {
            Optional<TeamRecruitmentNotification> notification = notificationRepository
                .findByIdForOutbox(payload.notificationId());
            if (notification == null || notification.isEmpty()) {
                return Optional.empty();
            }
            if (!matchesPayload(notification.get(), payload)) {
                throw new InvalidPayloadException("NOTIFICATION_TARGET_MISMATCH");
            }
            return notification;
        }

        List<TeamRecruitmentNotification> notifications = notificationRepository.findForOutbox(
            payload.recipientId(),
            payload.type(),
            payload.targetType(),
            payload.recruitmentId(),
            payload.applicationId(),
            payload.chatRoomId()
        );
        if (notifications == null || notifications.isEmpty()) {
            return Optional.empty();
        }
        if (notifications.size() != 1) {
            throw new InvalidPayloadException("AMBIGUOUS_NOTIFICATION_TARGET");
        }
        return Optional.of(notifications.get(0));
    }

    private boolean matchesPayload(TeamRecruitmentNotification notification, OutboxPayload payload) {
        Integer notificationRecipientId = notification.getRecipient() == null
            ? null
            : notification.getRecipient().getId();
        Integer notificationRecruitmentId = notification.getRecruitment() == null
            ? null
            : notification.getRecruitment().getId();
        Integer notificationApplicationId = notification.getApplication() == null
            ? null
            : notification.getApplication().getId();
        Integer notificationChatRoomId = notification.getChatRoom() == null
            ? null
            : notification.getChatRoom().getId();
        return Objects.equals(notificationRecipientId, payload.recipientId())
            && notification.getType() == payload.type()
            && notification.getTargetType() == payload.targetType()
            && Objects.equals(notificationRecruitmentId, payload.recruitmentId())
            && Objects.equals(notificationApplicationId, payload.applicationId())
            && Objects.equals(notificationChatRoomId, payload.chatRoomId());
    }

    private String fallbackContent(TeamRecruitmentNotificationType type, String recruitmentTitle) {
        return switch (type) {
            case NEW_APPLICATION -> recruitmentTitle + "에 새로운 지원자가 있어요.";
            case APPLICATION_ACCEPTED -> recruitmentTitle + " 지원이 승인되었어요.";
            case APPLICATION_REJECTED -> recruitmentTitle + " 지원이 거절되었어요.";
            case RECRUITMENT_CLOSED -> recruitmentTitle + " 모집이 마감되었어요.";
            case RECRUITMENT_DELETED -> recruitmentTitle + " 모집글이 삭제되었어요.";
            case NEW_CHAT_MESSAGE -> recruitmentTitle + " 새 메시지가 도착했어요.";
            default -> throw new InvalidPayloadException("UNSUPPORTED_EVENT_TYPE");
        };
    }

    private Optional<String> text(JsonNode payload, String field) {
        JsonNode value = payload.get(field);
        return value != null && value.isTextual() && StringUtils.hasText(value.asText())
            ? Optional.of(value.asText())
            : Optional.empty();
    }

    private boolean isRetryable(FcmSendResponse response) {
        if (response == null) {
            return true;
        }
        String errorCode = response.errorCode() == null
            ? ""
            : response.errorCode().toUpperCase(Locale.ROOT);
        String messagingErrorCode = response.messagingErrorCode() == null
            ? ""
            : response.messagingErrorCode().toUpperCase(Locale.ROOT);
        return !PERMANENT_FCM_CODES.stream().anyMatch(code ->
            errorCode.contains(code) || messagingErrorCode.contains(code)
        );
    }

    private void complete(TeamRecruitmentOutboxLeaseService.OutboxClaim claim, String workerId) {
        if (!leaseService.complete(claim.id(), workerId)) {
            log.warn(
                "팀원 모집 outbox complete 소유권이 없습니다. eventId={}, workerId={}",
                claim.id(),
                workerId
            );
        }
    }

    private void fail(
        TeamRecruitmentOutboxLeaseService.OutboxClaim claim,
        String workerId,
        String reason,
        boolean retryable
    ) {
        if (!leaseService.fail(claim.id(), workerId, reason, retryable)) {
            log.warn(
                "팀원 모집 outbox fail 소유권이 없습니다. eventId={}, workerId={}",
                claim.id(),
                workerId
            );
        }
    }

    private String firstText(String first, String second, String fallback) {
        if (StringUtils.hasText(first)) {
            return first;
        }
        if (StringUtils.hasText(second)) {
            return second;
        }
        return fallback;
    }

    private String exceptionSummary(Exception exception) {
        String message = exception.getMessage();
        return firstText(message, exception.getClass().getSimpleName(), "FCM_SEND_FAILED");
    }

    private record OutboxPayload(
        TeamRecruitmentNotificationType type,
        TeamRecruitmentNotificationTargetType targetType,
        Integer recipientId,
        Integer recruitmentId,
        Integer applicationId,
        Integer chatRoomId,
        Integer notificationId
    ) {
        static OutboxPayload from(JsonNode payload) {
            TeamRecruitmentNotificationType type = enumValue(
                payload,
                "type",
                TeamRecruitmentNotificationType.class
            );
            TeamRecruitmentNotificationTargetType targetType = enumValue(
                payload,
                "target_type",
                TeamRecruitmentNotificationTargetType.class
            );
            Integer recipientId = integer(payload, "recipient_id");
            Integer recruitmentId = integer(payload, "recruitment_id");
            Integer notificationId = integer(payload, "notification_id");
            if (recipientId == null || recruitmentId == null) {
                throw new InvalidPayloadException("MISSING_NOTIFICATION_TARGET");
            }
            return new OutboxPayload(
                type,
                targetType,
                recipientId,
                recruitmentId,
                integer(payload, "application_id"),
                integer(payload, "chat_room_id"),
                notificationId
            );
        }

        private static Integer integer(JsonNode payload, String field) {
            JsonNode value = payload.get(field);
            if (value == null || value.isNull()) {
                return null;
            }
            if (!value.canConvertToInt() || !value.isIntegralNumber()) {
                throw new InvalidPayloadException("INVALID_" + field.toUpperCase(Locale.ROOT));
            }
            return value.intValue();
        }

        private static <E extends Enum<E>> E enumValue(JsonNode payload, String field, Class<E> type) {
            String value = payload.path(field).asText(null);
            if (!StringUtils.hasText(value)) {
                throw new InvalidPayloadException("MISSING_" + field.toUpperCase(Locale.ROOT));
            }
            try {
                return Enum.valueOf(type, value);
            } catch (IllegalArgumentException exception) {
                throw new InvalidPayloadException("INVALID_" + field.toUpperCase(Locale.ROOT));
            }
        }
    }

    private static final class InvalidPayloadException extends RuntimeException {

        private InvalidPayloadException(String message) {
            super(message);
        }
    }
}
