package in.koreatech.koin.domain.translation.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.translation.model.TranslationResource;
import in.koreatech.koin.domain.translation.model.TranslationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
public record SourceLanguageRequest(
    @NotNull @Schema(description = "원문 종류", example = "ARTICLE_CONTENT") TranslationType resourceType,
    @NotBlank @Pattern(regexp = "[1-9][0-9]*") @Size(max = 19)
    @Schema(description = "글·댓글·메시지 ID", example = "1") String resourceId,
    @Min(0) @Schema(description = "댓글·쪽지의 부모 글 ID, 상점 메뉴의 shopId 또는 주문 메뉴의 orderableShopId. 그 외 생략", nullable = true) Integer parentId,
    @Min(0) @Schema(description = "분실물·팀 모집 쪽지의 채팅방 ID. 그 외 생략", nullable = true) Integer roomId,
    @NotBlank @Schema(description = "현재 화면 언어. GET /languages의 code", example = "en-US") String targetLanguage
) {
    public TranslationResource resource() {
        return new TranslationResource(resourceType, resourceId, parentId == null ? 0 : parentId,
            roomId == null ? 0 : roomId);
    }
}
