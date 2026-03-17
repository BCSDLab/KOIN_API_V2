package in.koreatech.koin.admin.callvan.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.callvan.model.enums.CallvanReportProcessType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminCallvanReportProcessRequest(
    @Schema(description = "신고 처리 유형", example = "WARNING", requiredMode = REQUIRED)
    @NotNull(message = "process type is required")
    CallvanReportProcessType processType
) {
}
