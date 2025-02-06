package in.koreatech.koin.domain.community.article.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record LostItemArticleRequest(
    @NotNull(message = "분실물 종류는 필수로 입력해야 합니다.")
    @Schema(description = "분실물 종류", example = "신분증", requiredMode = REQUIRED)
    String category,

    @NotNull(message = "분실물 습득 장소는 필수로 입력해야 합니다.")
    @Schema(description = "습득 장소", example = "학생회관 앞", requiredMode = REQUIRED)
    String foundPlace,

    @NotNull(message = "분실물 습득 날짜는 필수로 입력해야 합니다.")
    @Schema(description = "습득 날짜", example = "2025-01-01", requiredMode = REQUIRED)
    LocalDate foundDate,

    @Size(max = 1000, message = "본문의 내용은 최대 1,000자까지만 입력할 수 있습니다.")
    @Schema(description = "본문", example = "학생회관 앞 계단에 …")
    String content,

    @Size(max = 10, message = "이미지는 최대 10개까지만 업로드할 수 있습니다.")
    @Schema(description = "분실물 사진")
    List<String> images,

    @Schema(description = "등록일", example = "2025-01-10", requiredMode = REQUIRED)
    LocalDate registeredAt,

    @Schema(description = "수정일", example = "2025-01-10 16:53:22", requiredMode = REQUIRED)
    LocalDate updatedAt
) {

}
