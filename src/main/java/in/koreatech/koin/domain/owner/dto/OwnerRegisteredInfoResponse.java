package in.koreatech.koin.domain.owner.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.owner.model.OwnerShop;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record OwnerRegisteredInfoResponse(
    @Schema(description = "상점명", example = "교촌치킨", requiredMode = REQUIRED)
    String shopName,

    @Schema(description = "상점전화번호", example = "0514445555", requiredMode = REQUIRED)
    String shopNumber
) {

    public static OwnerRegisteredInfoResponse from(OwnerShop ownerShop) {
        if (ownerShop == null) {
            return new OwnerRegisteredInfoResponse("", "");
        }

        return new OwnerRegisteredInfoResponse(
            ownerShop.getShopName(),
            ownerShop.getShopNumber()
        );
    }
}
