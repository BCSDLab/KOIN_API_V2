package in.koreatech.koin.admin.abtest.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record AbtestResponse(

    @Schema(description = "실험 ID", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "실험명", example = "식단 UI 실험", requiredMode = REQUIRED)
    String displayTitle,

    @Schema(description = "실험 생성자 이름", example = "홍길동", requiredMode = REQUIRED)
    String creator,

    @Schema(description = "팀명", example = "campus", requiredMode = REQUIRED)
    String team,

    @Schema(description = "실험 생성자 이름", example = "홍길동", requiredMode = REQUIRED)
    String status,

    @Schema(description = "실험 생성자 이름", example = "홍길동", requiredMode = REQUIRED)
    String winnerName,

    @Schema(description = "실험명(변수명)", example = "dining_ui_test", requiredMode = REQUIRED)
    String title,

    @Schema(description = "실험 내용", example = "식단 UI 변경에 따른 사용자 변화량 조사", requiredMode = NOT_REQUIRED)
    String description,

    List<InnerVariableRequest> variables
) {

    @JsonNaming(SnakeCaseStrategy.class)
    private record InnerVariableRequest(

        @Schema(description = "실험군 편입 비율", example = "33", requiredMode = REQUIRED)
        Integer rate,

        @Schema(description = "실험군 이름", example = "실험군 A", requiredMode = REQUIRED)
        String displayName,

        @Schema(description = "실험군 이름(변수명)", example = "A", requiredMode = REQUIRED)
        String name
    ) {
    }
}
