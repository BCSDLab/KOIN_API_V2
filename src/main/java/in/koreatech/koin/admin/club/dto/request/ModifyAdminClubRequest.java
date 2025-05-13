package in.koreatech.koin.admin.club.dto.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ModifyAdminClubRequest(
    @Schema(description = "동아리 이름", example = "BCSD Lab", requiredMode = REQUIRED)
    @NotEmpty(message = "동아리 이름은 필수 입력 사항입니다.")
    String name,

    @Schema(description = "동아리 사진 링크", example = "https://bcsdlab.com/static/img/logo.d89d9cc.png", requiredMode = REQUIRED)
    @NotNull(message = "동아리 사진은 필수 입력 사항입니다.")
    String imageUrl,

    @Schema(description = "동아리 분과 카테고리 ID", example = "1", requiredMode = REQUIRED)
    @NotNull(message = "동아리 분과 카테고리는 필수 입력 사항입니다.")
    Integer clubCategoryId,

    @Schema(description = "동아리 위치", example = "학생회관", requiredMode = REQUIRED)
    @NotNull(message = "동아리 위치는 필수 입력 사항입니다.")
    String location,

    @Schema(description = "동아리 소개", example = "즐겁게 일하고 열심히 노는 IT 특성화 동아리", requiredMode = REQUIRED)
    @NotNull(message = "동아리 소개는 필수 입력 사항입니다.")
    String description,

    @Schema(description = "동아리 활성화 여부", example = "false", requiredMode = REQUIRED)
    @NotNull(message = "동아리 활성화 여부는 필수 입력 사항입니다.")
    Boolean active
) {

}
