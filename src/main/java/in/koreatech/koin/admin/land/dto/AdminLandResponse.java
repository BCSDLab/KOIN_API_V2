package in.koreatech.koin.admin.land.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.land.model.Land;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminLandResponse(
    @Schema(description = "고유 id", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "이름", example = "금실타운", requiredMode = REQUIRED)
    String name,

    @Schema(description = "종류", example = "원룸")
    String roomType,

    @Schema(description = "월세", example = "200만원 (6개월)")
    String monthlyFee,

    @Schema(description = "전세", example = "3500")
    String charterFee,

    @Schema(description = "삭제(soft delete) 여부", example = "false", requiredMode = REQUIRED)
    Boolean isDeleted
) {
    public static AdminLandResponse from(Land land) {
        return new AdminLandResponse(
            land.getId(),
            land.getName(),
            land.getRoomType(),
            land.getMonthlyFee(),
            land.getCharterFee(),
            land.isDeleted()
        );
    }
}
