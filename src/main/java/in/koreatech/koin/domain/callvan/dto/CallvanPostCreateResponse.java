package in.koreatech.koin.domain.callvan.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.domain.callvan.model.enums.CallvanStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record CallvanPostCreateResponse(
    @Schema(description = "게시글 ID", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "작성자 닉네임", example = "코룡이", requiredMode = REQUIRED)
    String author,

    @Schema(description = "출발지 타입", example = "FRONT_GATE", requiredMode = REQUIRED)
    String departureType,

    @Schema(description = "출발지 직접 입력 내용", example = "복지관", requiredMode = REQUIRED)
    String departureCustomName,

    @Schema(description = "도착지 타입", example = "TERMINAL", requiredMode = REQUIRED)
    String arrivalType,

    @Schema(description = "도착지 직접 입력 내용", example = "야우리", requiredMode = REQUIRED)
    String arrivalCustomName,

    @Schema(description = "출발일", example = "2024-03-25", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate departureDate,

    @Schema(description = "출발 시각", example = "15:30", requiredMode = REQUIRED)
    @JsonFormat(pattern = "HH:mm")
    LocalTime departureTime,

    @Schema(description = "최대 인원", example = "4", requiredMode = REQUIRED)
    Integer maxParticipants,

    @Schema(description = "현재 인원", example = "1", requiredMode = REQUIRED)
    Integer currentParticipants,

    @Schema(description = "상태", example = "RECRUITING", requiredMode = REQUIRED)
    CallvanStatus status,

    @Schema(description = "생성 일시", example = "2024-03-25 15:30:00", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt,

    @Schema(description = "수정 일시", example = "2024-03-25 15:30:00", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt
) {

    public static CallvanPostCreateResponse from(CallvanPost post) {
        return new CallvanPostCreateResponse(
            post.getId(),
            post.getAuthor().getNickname(),
            post.getDepartureType().name(),
            post.getDepartureCustomName(),
            post.getArrivalType().name(),
            post.getArrivalCustomName(),
            post.getDepartureDate(),
            post.getDepartureTime(),
            post.getMaxParticipants(),
            post.getCurrentParticipants(),
            post.getStatus(),
            post.getCreatedAt(),
            post.getUpdatedAt());
    }
}
