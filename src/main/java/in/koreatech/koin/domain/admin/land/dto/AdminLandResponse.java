package in.koreatech.koin.domain.admin.land.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.land.model.Land;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AdminLandResponse(
    @Schema(description = "고유 id", example = "1", required = true)
    Integer id,

    @Schema(description = "이름", example = "금실타운", required = true)
    String name,

    @Schema(description = "종류", example = "원룸")
    String roomType,

    @Schema(description = "월세", example = "200만원 (6개월)")
    String monthlyFee,

    @Schema(description = "전세", example = "3500")
    String charterFee,

    @Schema(description = "삭제(soft delete) 여부", example = "false", required = true)
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
