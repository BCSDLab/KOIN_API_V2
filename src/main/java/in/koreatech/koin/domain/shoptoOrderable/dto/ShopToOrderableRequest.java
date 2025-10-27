package in.koreatech.koin.domain.shoptoOrderable.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


@JsonNaming(SnakeCaseStrategy.class)
public record ShopToOrderableRequest(

    @Schema(description = "최소 주문 금액", example = "5000", requiredMode = REQUIRED)
    @NotNull(message = "최소 주문 금액은 필수입니다.")
    @Min(value = 0, message = "최소 주문 금액은 0원 이상이어야 합니다.")
    Integer minimumOrderAmount,

    @Schema(description = "포장 가능 여부", example = "true", requiredMode = REQUIRED)
    @NotNull(message = "포장 가능 여부는 필수입니다.")
    Boolean takeout,

    @Schema(description = "배달 옵션 (CAMPUS/OUTSIDE/BOTH)", example = "BOTH", requiredMode = REQUIRED)
    @NotNull(message = "배달 옵션은 필수입니다.")
    String deliveryOption,

    @Schema(description = "교내 배달팁", example = "1000", requiredMode = REQUIRED)
    @NotNull(message = "교내 배달팁은 필수입니다.")
    @Min(value = 0, message = "교내 배달팁은 0원 이상이어야 합니다.")
    Integer campusDeliveryTip,

    @Schema(description = "교외 배달팁", example = "2000", requiredMode = REQUIRED)
    @NotNull(message = "교외 배달팁은 필수입니다.")
    @Min(value = 0, message = "교외 배달팁은 0원 이상이어야 합니다.")
    Integer outsideDeliveryTip,

    @Schema(description = "사업자 등록증 URL", example = "https://example.com/business_license.jpg", requiredMode = REQUIRED)
    @NotBlank(message = "사업자 등록증 URL은 필수입니다.")
    String businessLicenseUrl,

    @Schema(description = "영업 신고증 URL", example = "https://example.com/business_certificate.jpg", requiredMode = REQUIRED)
    @NotBlank(message = "영업 신고증 URL은 필수입니다.")
    String businessCertificateUrl,

    @Schema(description = "통장 사본 URL", example = "https://example.com/bank_copy.jpg", requiredMode = REQUIRED)
    @NotBlank(message = "통장 사본 URL은 필수입니다.")
    String bankCopyUrl,

    @Schema(description = "은행명", example = "국민은행", requiredMode = REQUIRED)
    @NotBlank(message = "은행명은 필수입니다.")
    @Size(max = 10, message = "은행명은 10자 이하여야 합니다.")
    String bank,

    @Schema(description = "계좌번호", example = "123-456-789", requiredMode = REQUIRED)
    @NotBlank(message = "계좌번호는 필수입니다.")
    @Size(max = 20, message = "계좌번호는 20자 이하여야 합니다.")
    String accountNumber
) {
}
