package in.koreatech.koin.domain.callvan.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.RandomStringUtils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.callvan.model.CallvanParticipant;
import in.koreatech.koin.domain.callvan.model.CallvanPost;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record CallvanPostDetailResponse(
    @Schema(description = "게시글 ID", example = "1")
    Integer id,

    @Schema(description = "제목", example = "복지관 -> 천안역")
    String title,

    @Schema(description = "출발지", example = "복지관")
    String departure,

    @Schema(description = "도착지", example = "천안역")
    String arrival,

    @Schema(description = "출발 날짜", example = "2024-03-24")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate departureDate,

    @Schema(description = "출발 시간", example = "14:00")
    @JsonFormat(pattern = "HH:mm")
    LocalTime departureTime,

    @Schema(description = "현재 모집 인원", example = "2")
    Integer currentParticipants,

    @Schema(description = "최대 모집 인원", example = "4")
    Integer maxParticipants,

    @Schema(description = "모집 상태", example = "RECRUITING")
    String status,

    @Schema(description = "참여자 목록")
    List<CallvanParticipantResponse> participants
) {
    public static CallvanPostDetailResponse from(CallvanPost post, Integer userId) {
        String departureName = post.getDepartureType().getName();
        if (post.getDepartureCustomName() != null && !post.getDepartureCustomName().isBlank()) {
            departureName = post.getDepartureCustomName();
        }

        String arrivalName = post.getArrivalType().getName();
        if (post.getArrivalCustomName() != null && !post.getArrivalCustomName().isBlank()) {
            arrivalName = post.getArrivalCustomName();
        }

        return new CallvanPostDetailResponse(
            post.getId(),
            post.getTitle(),
            departureName,
            arrivalName,
            post.getDepartureDate(),
            post.getDepartureTime(),
            post.getCurrentParticipants(),
            post.getMaxParticipants(),
            post.getStatus().name(),
            post.getParticipants().stream()
                .map(it -> CallvanParticipantResponse.from(it, userId))
                .toList()
        );
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record CallvanParticipantResponse(
        @Schema(description = "참여자 ID", example = "1")
        Integer userId,

        @Schema(description = "참여자 닉네임", example = "익명_12345")
        String nickname,

        @Schema(description = "본인 여부", example = "false")
        Boolean is_me
    ) {
        public static CallvanParticipantResponse from(CallvanParticipant participant, Integer userId) {
            String nickname = participant.getMember().getNickname();
            if (nickname == null) {
                nickname = participant.getMember().getAnonymousNickname();
            }
            if (nickname == null) {
                nickname = "익명_" + RandomStringUtils.randomAlphabetic(13);
            }
            Integer participantId = participant.getMember().getId();
            return new CallvanParticipantResponse(
                participant.getMember().getId(),
                nickname,
                Objects.equals(userId, participantId)
            );
        }
    }
}
