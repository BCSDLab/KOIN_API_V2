package in.koreatech.koin.admin.student.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

import java.util.Objects;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.common.model.Criteria;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record StudentsCondition (
    @Schema(description = "페이지", example = "1", defaultValue = "1", requiredMode = NOT_REQUIRED)
    Integer page,

    @Schema(description = "페이지당 조회할 최대 개수", example = "10", defaultValue = "10", requiredMode = NOT_REQUIRED)
    Integer limit,

    @Schema(description = "인증 되었는지 여부", requiredMode = NOT_REQUIRED)
    Boolean isAuthed,

    @Schema(description = "닉네임", requiredMode = NOT_REQUIRED)
    String nickname,

    @Schema(description = "이메일", requiredMode = NOT_REQUIRED)
    String email
) {

    public StudentsCondition {
        if (Objects.isNull(page)) {
            page = Criteria.DEFAULT_PAGE;
        }
        if (Objects.isNull(limit)) {
            limit = Criteria.DEFAULT_LIMIT;
        }
    }
}
