package in.koreatech.koin.admin.lostItem.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.review.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AdminModifyLostItemArticleReportStatusRequest(
    @Schema(description = "신고 상태", example = "DISMISSED", requiredMode = REQUIRED)
    @NotNull(message = "변경하려는 상태는 필수입니다.")
    ReportStatus reportStatus
) {

}
