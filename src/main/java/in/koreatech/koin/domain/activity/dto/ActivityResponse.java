package in.koreatech.koin.domain.activity.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.activity.model.Activity;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ActivityResponse(
    @Schema(description = "활동 날짜", example = "2019-07-29")
    // String date,
    LocalDate date,

    @Schema(description = "삭제 여부", example = "false")
    Boolean isDeleted,

    @Schema(description = "최근 업데이트 일시", example = "2019-08-16 23:01:52")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt,

    @Schema(description = "초기 생성 일시", example = "2019-08-16 23:01:52")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt,

    @Schema(description = "활동 설명", example = "더 편리한 서비스 제공을 위해 시간표 기능을 추가했습니다.")
    String description,

    @Schema(description = "이미지 URL 목록", example = "null")
    List<String> imageUrls,

    @Schema(description = "고유 식별자", example = "1")
    Long id,

    @Schema(description = "제목", example = "코인 시간표 기능 추가")
    String title
) {
    public static ActivityResponse from(Activity activity, List<String> imageUrls) {
        return new ActivityResponse(
            activity.getDate(),
            activity.getIsDeleted(),
            activity.getUpdatedAt(),
            activity.getCreatedAt(),
            activity.getDescription(),
            imageUrls,
            activity.getId(),
            activity.getTitle()
        );
    }
}
