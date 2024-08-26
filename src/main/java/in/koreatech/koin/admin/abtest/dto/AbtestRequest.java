package in.koreatech.koin.admin.abtest.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonNaming(SnakeCaseStrategy.class)
public record AbtestRequest(

    @NotBlank(message = "실험명은 필수입니다.")
    @Schema(description = "실험명", example = "식단 UI 실험", requiredMode = REQUIRED)
    String displayTitle,

    @Schema(description = "실험 생성자 이름", example = "홍길동", requiredMode = REQUIRED)
    String creater,

    @Schema(description = "팀명", example = "campus", requiredMode = REQUIRED)
    String team,

    @NotBlank(message = "실험명(변수명)은 필수입니다.")
    @Schema(description = "실험명(변수명)", example = "dining_ui_test", requiredMode = REQUIRED)
    String title,

    @Schema(description = "실험 내용", example = "식단 UI 변경에 따른 사용자 변화량 조사", requiredMode = REQUIRED)
    String description,

    List<InnerVariableRequest> variables
) {

    @JsonNaming(SnakeCaseStrategy.class)
    public record InnerVariableRequest(

        @NotNull(message = "실험군 편입 비율은 필수입니다.")
        @Schema(description = "실험군 편입 비율", example = "33", requiredMode = REQUIRED)
        Integer rate,

        @NotBlank(message = "실험군 이름은 필수입니다.")
        @Schema(description = "실험군 이름", example = "실험군 A", requiredMode = REQUIRED)
        String displayName,

        @NotBlank(message = "실험군 이름(변수명)은 필수입니다.")
        @Schema(description = "실험군 이름(변수명)", example = "A", requiredMode = REQUIRED)
        String name
    ) {
    }
}
