package in.koreatech.koin.domain.land.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.land.model.Land;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record LandsResponse(
    @Schema(description = "상세 이름", example = "럭키빌(투베이)", requiredMode = NOT_REQUIRED)
    String internalName,

    @Schema(description = "월세 - MVP 개편으로 항상 null (예전 앱 호환을 위해 키 유지)", example = "null", requiredMode = NOT_REQUIRED)
    String monthlyFee,

    @Schema(description = "위도", example = "36.769204", requiredMode = NOT_REQUIRED)
    Double latitude,

    @Schema(description = "전세 - MVP 개편으로 항상 null (예전 앱 호환을 위해 키 유지)", example = "null", requiredMode = NOT_REQUIRED)
    String charterFee,

    @Schema(description = "이름", example = "럭키빌(투베이)", requiredMode = NOT_REQUIRED)
    String name,

    @Schema(description = "복덕방 고유 ID", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "경도", example = "127.282554", requiredMode = NOT_REQUIRED)
    Double longitude,

    @Schema(description = "방 종류", example = "투베이", requiredMode = NOT_REQUIRED)
    String roomType,

    @Schema(description = "주소", example = "충청남도 천안시 동남구 병천면", requiredMode = NOT_REQUIRED)
    String address,

    @Schema(description = "월세 매물 여부 - 가격 대신 매물 종류 표시용", example = "true", requiredMode = REQUIRED)
    boolean isMonthlyAvailable,

    @Schema(description = "전세 매물 여부 - 가격 대신 매물 종류 표시용", example = "false", requiredMode = REQUIRED)
    boolean isCharterAvailable
) {

    public static LandsResponse from(Land land) {
        return new LandsResponse(
            land.getInternalName(),
            null,
            land.getLatitude(),
            null,
            land.getName(),
            land.getId(),
            land.getLongitude(),
            land.getRoomType(),
            land.getAddress(),
            StringUtils.hasText(land.getMonthlyFee()),
            StringUtils.hasText(land.getCharterFee())
        );
    }
}
