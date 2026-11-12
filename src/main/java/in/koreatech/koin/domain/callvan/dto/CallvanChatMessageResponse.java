package in.koreatech.koin.domain.callvan.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.callvan.model.CallvanChatMessage;
import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.domain.callvan.model.enums.CallvanLocation;
import in.koreatech.koin.domain.callvan.model.enums.CallvanMessageType;
import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.language.service.OfficialNameResolver;
import in.koreatech.koin.domain.translation.service.TranslationSourceFields;
import in.koreatech.koin.domain.translation.service.TranslationSourceVersion;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record CallvanChatMessageResponse(
    @Schema(description = "채팅방 이름", example = "별관동 -> 천안역 (14:00) (4명)")
    String roomName,

    @Schema(description = "메시지 목록")
    List<CallvanMessageDto> messages
) {

    public static CallvanChatMessageResponse of(CallvanPost post, List<CallvanChatMessage> messages, Integer userId,
        SupportedLanguage language) {
        String departure = officialName(post.getDepartureType().getName(), language);
        if (post.getDepartureType() == CallvanLocation.CUSTOM && StringUtils.hasText(post.getDepartureCustomName())) {
            departure = post.getDepartureCustomName();
        }

        String arrival = officialName(post.getArrivalType().getName(), language);
        if (post.getArrivalType() == CallvanLocation.CUSTOM && StringUtils.hasText(post.getArrivalCustomName())) {
            arrival = post.getArrivalCustomName();
        }

        String time = post.getDepartureTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        String roomName = String.format("%s -> %s (%s) (%s)",
            departure, arrival, time, participantCount(post.getMaxParticipants(), language));

        List<CallvanMessageDto> messageDtos = messages.stream()
            .map(msg -> CallvanMessageDto.from(msg, userId, language))
            .toList();

        return new CallvanChatMessageResponse(roomName, messageDtos);
    }

    private static String officialName(String original, SupportedLanguage language) {
        return OfficialNameResolver.stationName(OfficialNameResolver.buildingName(original, language), language);
    }

    private static String participantCount(int count, SupportedLanguage language) {
        return switch (language) {
            case KOREAN -> count + "명";
            case ENGLISH -> count + (count == 1 ? " person" : " people");
            case SIMPLIFIED_CHINESE, JAPANESE -> count + "人";
            case MONGOLIAN -> count + " хүн";
            case UZBEK -> count + " kishi";
            case BURMESE -> count + " ဦး";
        };
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record CallvanMessageDto(
        @Schema(description = "메시지 ID. 콜밴 메시지 번역 경로의 messageId로 사용합니다.", example = "1")
        Integer messageId,

        @Schema(description = "작성자 ID", example = "1")
        Integer userId,

        @Schema(description = "작성자 닉네임", example = "익명_12345")
        String senderNickname,

        @Schema(description = "메시지 내용", example = "안녕하세요!")
        String content,

        @Schema(description = "작성 날짜", example = "2024. 03. 24")
        String date,

        @Schema(description = "작성 시간. language에 맞춰 오전·오후를 표시합니다.", example = "오후 2:00")
        String time,

        @Schema(description = "이미지 여부", example = "false")
        Boolean isImage,

        @Schema(description = "퇴장 유저 여부", example = "false")
        Boolean isLeftUser,

        @Schema(description = "내 메시지 여부", example = "true")
        Boolean isMine
    ) {

        public static CallvanMessageDto from(CallvanChatMessage message, Integer currentUserId, SupportedLanguage language) {
            return new CallvanMessageDto(
                message.getId(),
                message.getSender().getId(),
                message.getSenderNickname(),
                message.getContent(),
                message.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy. MM. dd")),
                message.getCreatedAt().format(DateTimeFormatter.ofPattern("a h:mm")
                    .withLocale(Locale.forLanguageTag(language.getCode()))),
                Boolean.TRUE.equals(message.getIsImage()) || message.getMessageType() == CallvanMessageType.IMAGE,
                message.getIsLeftUser(),
                message.getSender().getId().equals(currentUserId));
        }

        @JsonProperty("translation_source_version")
        @Schema(description = "번역 요청의 expected_source_version에 그대로 전달할 원문 버전. 이미지면 null입니다.", nullable = true)
        public String getTranslationSourceVersion() {
            return Boolean.TRUE.equals(isImage) ? null
                : TranslationSourceVersion.of(TranslationSourceFields.message(content));
        }
    }
}
