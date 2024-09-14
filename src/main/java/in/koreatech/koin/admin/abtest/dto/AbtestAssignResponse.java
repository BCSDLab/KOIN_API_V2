package in.koreatech.koin.admin.abtest.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.admin.abtest.model.AbtestVariable;
import in.koreatech.koin.admin.abtest.model.AccessHistory;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record AbtestAssignResponse (
    @Schema(description = "편입된 변수")
    String variableName,

    @Schema(description = "기기 식별자")
    Integer accessHistoryId
) {

    public static AbtestAssignResponse of(AbtestVariable abtestVariable, AccessHistory accessHistory) {
        return new AbtestAssignResponse(abtestVariable.getName(), accessHistory.getId());
    }
}
