package in.koreatech.koin.admin.banner.dto.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminBannerCategoryDescriptionModifyRequest (
    @Schema(description = "배너 카테고리 설명", example = "140*112 앱/웹 랜딩시 뜨는 모달입니다.", requiredMode = REQUIRED)
    @Size(max = 50, message = "배너 카테고리 설명은 50자가 최대입니다.")
    String description
) {

}
