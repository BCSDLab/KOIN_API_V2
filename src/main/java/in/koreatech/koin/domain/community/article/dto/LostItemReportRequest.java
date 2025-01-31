package in.koreatech.koin.domain.community.article.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@JsonNaming(value = SnakeCaseStrategy.class)
public record LostItemReportRequest(
    @Schema(description = "신고내용 리스트", requiredMode = REQUIRED)
    @NotEmpty(message = "신고 항목은 최소 1개 이상 포함해야 합니다.")
    @Valid
    List<InnerLostItemReport> reports
) {

    public record InnerLostItemReport(
        @Schema(example = "개인정보", description = "신고 제목", requiredMode = REQUIRED)
        @NotBlank(message = "신고 제목은 필수입니다.")
        String title,

        @Schema(example = "개인정보가 포함된 리뷰입니다.", description = "신고 내용", requiredMode = REQUIRED)
        @NotBlank(message = "신고 내용은 필수입니다.")
        String content
    ) {
    }
}