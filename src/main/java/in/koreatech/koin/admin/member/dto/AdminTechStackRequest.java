package in.koreatech.koin.admin.member.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.member.model.TechStack;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminTechStackRequest(

    @Schema(description = "기술스택 고유 ID", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "이미지 링크", example = "http://url.com", requiredMode = REQUIRED)
    String imageUrl,

    @Schema(description = "기술 스택명", example = "Spring", requiredMode = REQUIRED)
    @NotBlank(message = "기술 스택명은 비워둘 수 없습니다.")
    String name,

    @Schema(description = "기술 스택 설명", example = "스프링은 웹 프레임워크이다", requiredMode = REQUIRED)
    String description,

    @Schema(description = "삭제 여부", example = "false")
    boolean isDeleted
) {

    public TechStack toEntity(Integer trackId) {
        return TechStack.builder()
            .imageUrl(imageUrl)
            .trackId(trackId)
            .name(name)
            .description(description)
            .isDeleted(isDeleted)
            .build();
    }
}
