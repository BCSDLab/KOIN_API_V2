package in.koreatech.koin.domain.callvan.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.callvan.model.CallvanNotification;
import in.koreatech.koin.domain.callvan.model.enums.CallvanNotificationType;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record CallvanNotificationResponse(
    @Schema(description = "알림 ID", example = "1")
    Integer id,

    @Schema(description = "알림 타입", example = "RECRUITMENT_COMPLETE")
    CallvanNotificationType type,

    @Schema(description = "메시지 미리보기", example = "해당 콜벤팟 인원이 모두 모집되었습니다.")
    String messagePreview,

    @Schema(description = "읽음 여부", example = "false")
    Boolean isRead,

    @Schema(description = "생성 일시", example = "2024-03-24 14:00:00")
    LocalDateTime createdAt,

    @Schema(description = "콜벤 게시글 ID", example = "1")
    Integer postId,

    @Schema(description = "출발지", example = "복지관")
    String departure,

    @Schema(description = "도착지", example = "천안역")
    String arrival,

    @Schema(description = "출발 날짜", example = "2024-03-24")
    LocalDate departureDate,

    @Schema(description = "출발 시간", example = "14:00")
    LocalTime departureTime,

    @Schema(description = "현재 모집 인원", example = "2")
    Integer currentParticipants,

    @Schema(description = "최대 모집 인원", example = "4")
    Integer maxParticipants,

    @Schema(description = "발신자 닉네임", example = "익명_123")
    String senderNickname,

    @Schema(description = "참여자 닉네임", example = "익명_456")
    String joinedMemberNickname
) {

    public static CallvanNotificationResponse from(CallvanNotification notification) {
        String departureName = notification.getDepartureType().getName();
        if (StringUtils.hasText(notification.getDepartureCustomName())) {
            departureName = notification.getDepartureCustomName();
        }

        String arrivalName = notification.getArrivalType().getName();
        if (StringUtils.hasText(notification.getArrivalCustomName())) {
            arrivalName = notification.getArrivalCustomName();
        }

        return new CallvanNotificationResponse(
            notification.getId(),
            notification.getNotificationType(),
            notification.getMessagePreview(),
            notification.getIsRead(),
            notification.getCreatedAt(),
            notification.getPost() != null ? notification.getPost().getId() : null,
            departureName,
            arrivalName,
            notification.getDepartureDate(),
            notification.getDepartureTime(),
            notification.getCurrentParticipants(),
            notification.getMaxParticipants(),
            notification.getSenderNickname(),
            notification.getJoinedMemberNickname()
        );
    }
}
