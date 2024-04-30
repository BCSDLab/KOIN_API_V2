package in.koreatech.koin.domain.activity.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.activity.model.Activity;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ActivityResponse(
    @Schema(description = "활동 날짜", example = "2019-07-29", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate date,

    @Schema(description = "삭제 여부", example = "false", requiredMode = REQUIRED)
    Boolean isDeleted,

    @Schema(description = "최근 업데이트 일시", example = "2019-08-16 23:01:52", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt,

    @Schema(description = "초기 생성 일시", example = "2019-08-16 23:01:52", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt,

    @Schema(description = "활동 설명", example = "더 편리한 서비스 제공을 위해 시간표 기능을 추가했습니다.", requiredMode = REQUIRED)
    String description,

    @Schema(description = "이미지 URL 목록", example = """
        ["https://test2.com.png", "https://test3.com.png"]
        """, requiredMode = NOT_REQUIRED)
    List<String> imageUrls,

    @Schema(description = "고유 식별자", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "제목", example = "코인 시간표 기능 추가", requiredMode = REQUIRED)
    String title
) {

    public static ActivityResponse of(Activity activity, List<String> imageUrls) {
        return new ActivityResponse(
            activity.getDate(),
            activity.isDeleted(),
            activity.getUpdatedAt(),
            activity.getCreatedAt(),
            activity.getDescription(),
            imageUrls,
            activity.getId(),
            activity.getTitle()
        );
    }
}
