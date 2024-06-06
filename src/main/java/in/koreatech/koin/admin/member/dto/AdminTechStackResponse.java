package in.koreatech.koin.admin.member.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.member.model.TechStack;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminTechStackResponse(

    @Schema(description = "기술스택 고유 ID", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "이미지 링크", example = "http://url.com", requiredMode = REQUIRED)
    String imageUrl,

    @Schema(description = "기술 스택명", example = "Spring", requiredMode = REQUIRED)
    String name,

    @Schema(description = "기술 스택 설명", example = "스프링은 웹 프레임워크이다", requiredMode = REQUIRED)
    String description,

    @Schema(description = "트랙 고유 ID", example = "2", requiredMode = REQUIRED)
    Integer trackId,

    @Schema(description = "삭제 여부", example = "false", requiredMode = REQUIRED)
    Boolean isDeleted,

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "생성 일자", example = "2024-01-15 12:00:00", requiredMode = REQUIRED)
    LocalDateTime createdAt,

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "수정 일자", example = "2024-01-15 12:00:00", requiredMode = REQUIRED)
    LocalDateTime updatedAt
) {

    public static AdminTechStackResponse from(TechStack techStack) {
        return new AdminTechStackResponse(
            techStack.getId(),
            techStack.getImageUrl(),
            techStack.getName(),
            techStack.getDescription(),
            techStack.getTrackId(),
            techStack.isDeleted(),
            techStack.getCreatedAt(),
            techStack.getUpdatedAt()
        );
    }
}
