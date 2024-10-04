package in.koreatech.koin.domain.coopshop.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.coopshop.model.CoopShopSemester;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record CoopShopsResponse(
    @Schema(example = "24-2학기", description = "운영 학기", requiredMode = REQUIRED)
    String semester,

    @Schema(example = "2024-09-02", description = "학기 기간 시작일", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate fromDate,

    @Schema(example = "2024-12-20", description = "학기 기간 마감일", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate toDate,

    @Schema(example = "하계방학", description = "운영 학기", requiredMode = REQUIRED)
    List<CoopShopResponse> coopShops
) {

    public static CoopShopsResponse from(CoopShopSemester coopShopSemester) {
        return new CoopShopsResponse(
            coopShopSemester.getSemester(),
            coopShopSemester.getFromDate(),
            coopShopSemester.getToDate(),
            coopShopSemester.getCoopShops().stream()
                .map(CoopShopResponse::from)
                .toList()
        );
    }

}
