package in.koreatech.koin.domain.callvan.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.callvan.model.CallvanChatMessage;
import in.koreatech.koin.domain.callvan.model.CallvanPost;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record CallvanChatMessageResponse(
    @Schema(description = "채팅방 이름", example = "별관동 -> 천안역 (14:00) (4명)")
    String roomName,

    @Schema(description = "메시지 목록")
    List<CallvanMessageDto> messages
) {
    public static CallvanChatMessageResponse of(CallvanPost post, List<CallvanChatMessage> messages, Integer userId) {
        String departure = post.getDepartureType().getName();
        if (post.getDepartureCustomName() != null && !post.getDepartureCustomName().isBlank()) {
            departure = post.getDepartureCustomName();
        }

        String arrival = post.getArrivalType().getName();
        if (post.getArrivalCustomName() != null && !post.getArrivalCustomName().isBlank()) {
            arrival = post.getArrivalCustomName();
        }

        String time = post.getDepartureTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        String roomName = String.format("%s -> %s (%s) (%d명)",
            departure, arrival, time, post.getMaxParticipants());

        List<CallvanMessageDto> messageDtos = messages.stream()
            .map(msg -> CallvanMessageDto.from(msg, userId))
            .toList();

        return new CallvanChatMessageResponse(roomName, messageDtos);
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record CallvanMessageDto(
        @Schema(description = "작성자 ID", example = "1")
        Integer userId,

        @Schema(description = "작성자 닉네임", example = "익명_12345")
        String senderNickname,

        @Schema(description = "메시지 내용", example = "안녕하세요!")
        String content,

        @Schema(description = "작성 날짜", example = "2024. 03. 24")
        String date,

        @Schema(description = "작성 시간", example = "오후 2:00")
        String time,

        @Schema(description = "이미지 여부", example = "false")
        Boolean isImage,

        @Schema(description = "퇴장 유저 여부", example = "false")
        Boolean isLeftUser,

        @Schema(description = "내 메시지 여부", example = "true")
        Boolean isMine
    ) {
        public static CallvanMessageDto from(CallvanChatMessage message, Integer currentUserId) {
            return new CallvanMessageDto(
                message.getSender().getId(),
                message.getSenderNickname(),
                message.getContent(),
                message.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy. MM. dd")),
                message.getCreatedAt().format(DateTimeFormatter.ofPattern("a h:mm").withLocale(Locale.KOREAN)),
                message.getIsImage(),
                message.getIsLeftUser(),
                message.getSender().getId().equals(currentUserId));
        }
    }
}
