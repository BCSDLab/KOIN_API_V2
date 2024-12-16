package in.koreatech.koin.admin.abtest.dto.response;

import in.koreatech.koin.admin.abtest.model.AccessHistory;
import io.swagger.v3.oas.annotations.media.Schema;

public record AbtestAccessHistoryResponse(
    @Schema(description = "기기 식별자")
    Integer accessHistoryId
) {

    public static AbtestAccessHistoryResponse from(AccessHistory accessHistory) {
        return new AbtestAccessHistoryResponse(accessHistory.getId());
    }
}
