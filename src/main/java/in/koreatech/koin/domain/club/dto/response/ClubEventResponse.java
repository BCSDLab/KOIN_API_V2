package in.koreatech.koin.domain.club.dto.response;

import com.fasterxml.jackson.core.type.TypeReference;
import static com.fasterxml.jackson.databind.PropertyNamingStrategies.*;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.club.enums.ClubEventStatus;
import in.koreatech.koin.domain.club.model.ClubEvent;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record ClubEventResponse(
    @Schema(description = "동아리 행사 ID", example = "12")
    Integer id,

    @Schema(description = "행사 이름", example = "B-CON")
    String name,

    @Schema(description = "행사 이미지 목록", example = """
    ["https://image1.com", "https://image2.com"]
    """, requiredMode = NOT_REQUIRED)
    List<String> imageUrls,

    @Schema(description = "행사 시작일", example = "2025-07-01T09:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime startDate,

    @Schema(description = "행사 종료일", example = "2025-07-02T18:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime endDate,

    @Schema(description = "행사 소개 (요약)", example = "BCSDLab의 멘토 혹은 레귤러들의 경험을 공유해요.")
    String introduce,

    @Schema(description = "행사 상세 내용", example = "여러 동아리원들과 자신의 생각, 경험에 대해 나눠요,")
    String content,

    @Schema(description = "현재 행사 상태", example = "UPCOMING")
    String status
) {

    public static ClubEventResponse from(ClubEvent event, LocalDateTime now) {
        return new ClubEventResponse(
            event.getId(),
            event.getName(),
            parseImageUrls(event.getImageUrls()),
            event.getStartDate(),
            event.getEndDate(),
            event.getIntroduce(),
            event.getContent(),
            calculateStatus(event.getStartDate(), event.getEndDate(), now).getDisplayName()
        );
    }

    public static ClubEventStatus calculateStatus(LocalDateTime start, LocalDateTime end, LocalDateTime now) {
        if (now.isBefore(start.minusHours(1))) {
            return ClubEventStatus.UPCOMING;
        }
        if (now.isBefore(start)) {
            return ClubEventStatus.SOON;
        }
        if (now.isBefore(end.plusMinutes(1))) {
            return ClubEventStatus.ONGOING;
        }
        return ClubEventStatus.ENDED;
    }

    private static List<String> parseImageUrls(String jsonArray) {
        if (jsonArray == null || jsonArray.isBlank()) return List.of();
        try {
            return new ObjectMapper().readValue(jsonArray, new TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
