package in.koreatech.koin.domain.callvan.dto;

import in.koreatech.koin.domain.language.model.SupportedLanguage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    List<CallvanParticipantResponse> participants,

    @Schema(description = "표시용 제목. 원문 title은 유지합니다.") String displayTitle,
    @Schema(description = "표시용 출발지. 직접 입력 장소는 원문 유지") String displayDeparture,
    @Schema(description = "표시용 도착지. 직접 입력 장소는 원문 유지") String displayArrival
) {

    public CallvanPostDetailResponse(Integer id, String title, String departure, String arrival,
        LocalDate departureDate, LocalTime departureTime, Integer currentParticipants, Integer maxParticipants,
        String status, List<CallvanParticipantResponse> participants) {
        this(id, title, departure, arrival, departureDate, departureTime, currentParticipants, maxParticipants,
            status, participants, title, departure, arrival);
    }

    @com.fasterxml.jackson.annotation.JsonProperty("translation_source_version")
    @Schema(description = "모집글 번역 요청의 expected_source_version으로 전달할 버전")
    public String getTranslationSourceVersion() {
        return in.koreatech.koin.domain.translation.service.TranslationSourceVersion.of(
            in.koreatech.koin.domain.translation.service.TranslationSourceFields.callvanPost(title, departure, arrival));
    }

    public static CallvanPostDetailResponse from(CallvanPost post, Integer userId, Set<Integer> reportedUserIds) {
        return from(post, userId, reportedUserIds, SupportedLanguage.DEFAULT);
    }

    public static CallvanPostDetailResponse from(CallvanPost post, Integer userId, Set<Integer> reportedUserIds,
        SupportedLanguage language) {
        String departureName = post.getDepartureType().getName();
        if (StringUtils.hasText(post.getDepartureCustomName())) {
            departureName = post.getDepartureCustomName();
        }

        String arrivalName = post.getArrivalType().getName();
        if (StringUtils.hasText(post.getArrivalCustomName())) {
            arrivalName = post.getArrivalCustomName();
        }

        String displayDeparture = CallvanPostSearchResponse.displayLocation(post.getDepartureType(), departureName, language);
        String displayArrival = CallvanPostSearchResponse.displayLocation(post.getArrivalType(), arrivalName, language);
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
                .filter(p -> !p.getIsDeleted())
                .map(it -> CallvanParticipantResponse.from(it, userId, reportedUserIds))
                .toList(), displayDeparture + " -> " + displayArrival, displayDeparture, displayArrival);
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record CallvanParticipantResponse(
        @Schema(description = "참여자 ID", example = "1")
        Integer userId,

        @Schema(description = "참여자 닉네임", example = "익명_12345")
        String nickname,

        @Schema(description = "본인 여부", example = "false")
        Boolean isMe,

        @Schema(description = "신고 접수 여부", example = "false")
        Boolean isReported
    ) {

        public static CallvanParticipantResponse from(
            CallvanParticipant participant, Integer userId, Set<Integer> reportedUserIds
        ) {
            Integer participantId = participant.getMember().getId();
            String nickname = participant.getMember().getDisplayNickname();
            return new CallvanParticipantResponse(
                participantId,
                nickname,
                Objects.equals(userId, participantId),
                reportedUserIds.contains(participantId)
            );
        }
    }
}
