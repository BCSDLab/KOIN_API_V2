package in.koreatech.koin.admin.ownershop.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequest;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestDeliveryOption;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminShopOrderServiceResponse(
    @Schema(description = "고유 id", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "상점 id", requiredMode = REQUIRED)
    Integer shopId,

    @Schema(description = "상점명", requiredMode = REQUIRED)
    String shopName,

    @Schema(description = "최소 주문 금액", example = "10000", requiredMode = REQUIRED)
    Integer minimumOrderAmount,

    @Schema(description = "포장 가능 여부", example = "true", requiredMode = REQUIRED)
    Boolean isTakeout,

    @Schema(description = "배달 옵션", example = "CAMPUS", requiredMode = REQUIRED)
    ShopOrderServiceRequestDeliveryOption deliveryOption,

    @Schema(description = "교내 배달비", example = "2000", requiredMode = REQUIRED)
    Integer campusDeliveryTip,

    @Schema(description = "교외 배달비", example = "3000", requiredMode = NOT_REQUIRED)
    Integer offCampusDeliveryTip,

    @Schema(description = "요청 상태", example = "PENDING", requiredMode = REQUIRED)
    ShopOrderServiceRequestStatus requestStatus,

    @Schema(description = "사업자등록증 URL", requiredMode = REQUIRED)
    String businessLicenseUrl,

    @Schema(description = "사업자등록 증명서 URL", requiredMode = REQUIRED)
    String businessCertificateUrl,

    @Schema(description = "통장 사본 URL", requiredMode = REQUIRED)
    String bankCopyUrl,

    @Schema(description = "은행명", example = "국민은행", requiredMode = REQUIRED)
    String bank,

    @Schema(description = "계좌번호", example = "123-456-789", requiredMode = REQUIRED)
    String accountNumber,

    @Schema(description = "신청 일자", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt,

    @Schema(description = "승인 일자", requiredMode = NOT_REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime approvedAt
) {
    public static AdminShopOrderServiceResponse from(ShopOrderServiceRequest shopOrderServiceRequest) {
        return new AdminShopOrderServiceResponse(
            shopOrderServiceRequest.getId(),
            shopOrderServiceRequest.getShop().getId(),
            shopOrderServiceRequest.getShop().getName(),
            shopOrderServiceRequest.getMinimumOrderAmount(),
            shopOrderServiceRequest.getIsTakeout(),
            shopOrderServiceRequest.getDeliveryOption(),
            shopOrderServiceRequest.getCampusDeliveryTip(),
            shopOrderServiceRequest.getOffCampusDeliveryTip(),
            shopOrderServiceRequest.getRequestStatus(),
            shopOrderServiceRequest.getBusinessLicenseUrl(),
            shopOrderServiceRequest.getBusinessCertificateUrl(),
            shopOrderServiceRequest.getBankCopyUrl(),
            shopOrderServiceRequest.getBank(),
            shopOrderServiceRequest.getAccountNumber(),
            shopOrderServiceRequest.getCreatedAt(),
            shopOrderServiceRequest.getApprovedAt()
        );
    }
}

