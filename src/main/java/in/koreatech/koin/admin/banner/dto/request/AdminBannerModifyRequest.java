package in.koreatech.koin.admin.banner.dto.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminBannerModifyRequest(
    @Schema(description = "배너 제목", example = "천원의 아침", requiredMode = REQUIRED)
    @NotBlank(message = "배너 제목은 공백일 수 없습니다")
    @Size(max = 255, message = "배너 제목은 최대 255자 입니다.")
    String title,

    @Schema(description = "배너 이미지 링크", example = "https://example.com/1000won.jpg", requiredMode = REQUIRED)
    @NotBlank(message = "배너 이미지 링크는 공백일 수 없습니다")
    @Size(max = 255, message = "배너 이미지 링크는 최대 255자 입니다.")
    String imageUrl,

    @Pattern(regexp = "^https://[\\w\\-_.:/?=&%]+$", message = "웹 리다이렉션 링크는 https://로 시작해야 합니다.")
    @Schema(description = "웹 리다이렉션 링크", example = "https://example.com/1000won", requiredMode = NOT_REQUIRED)
    @Size(max = 255, message = "웹 리다이렉션 링크는 최대 255자 입니다.")
    String webRedirectLink,

    @Pattern(regexp = "^koin://[\\w\\-_.:/?=&%]+", message = "안드로이드 딥링크는 koin://으로 시작해야 합니다.")
    @Schema(description = "안드로이드 리다이렉션 링크", example = "koin://example", requiredMode = NOT_REQUIRED)
    @Size(max = 255, message = "안드로이드 리다이렉션 링크는 최대 255자 입니다.")
    String androidRedirectLink,

    @Pattern(regexp = "^[0-9.]+$", message = "버전은 숫자와 점(.)만 입력 가능합니다.")
    @Schema(description = "안드로이드 최소 버전", example = "3.0.14", requiredMode = NOT_REQUIRED)
    @Size(max = 50, message = "안드로이드 최소 버전은 최대 50자 입니다.")
    String androidMinimumVersion,

    @Schema(description = "ios 리다이렉션 링크", example = "https://example.com/1000won", requiredMode = NOT_REQUIRED)
    @Size(max = 255, message = "ios 리다이렉션 링크는 최대 255자 입니다.")
    String iosRedirectLink,

    @Pattern(regexp = "^[0-9.]+$", message = "버전은 숫자와 점(.)만 입력 가능합니다.")
    @Schema(description = "ios 최소 버전", example = "3.0.14", requiredMode = NOT_REQUIRED)
    @Size(max = 50, message = "ios 최소 버전은 최대 50자 입니다.")
    String iosMinimumVersion,

    @Schema(description = "활성화 여부", example ="true", requiredMode = REQUIRED)
    @NotNull(message = "활성화 여부는 필수입니다.")
    Boolean isActive
) {
}
