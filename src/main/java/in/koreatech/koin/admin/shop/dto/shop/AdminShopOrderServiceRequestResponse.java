package in.koreatech.koin.admin.shop.dto.shop;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequest;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestDeliveryOption;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestStatus;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceTransferData;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminShopOrderServiceRequestResponse(
    @Schema(description = "신청 ID") Integer id,
    @Schema(description = "상점 ID") Integer shopId,
    @Schema(description = "상점명") String shopName,
    @Schema(description = "은행명") String bank,
    @Schema(description = "계좌번호") String accountNumber,
    @Schema(description = "최소 주문 금액") Integer minimumOrderAmount,
    @Schema(description = "포장 가능 여부") Boolean isTakeout,
    @Schema(description = "배달 가능 지역") ShopOrderServiceRequestDeliveryOption deliveryOption,
    @Schema(description = "공통 배달비. 이전 규격 신청은 null") Integer deliveryTip,
    @Schema(description = "사업자 등록증 URL") String businessLicenseUrl,
    @Schema(description = "영업 신고증 URL") String businessCertificateUrl,
    @Schema(description = "통장 사본 URL") String bankCopyUrl,
    @Schema(description = "신청 시 저장한 이관 데이터. 이전 규격 신청은 null") ShopOrderServiceTransferData transferData,
    @Schema(description = "신청 상태. PENDING인 신청만 수락 또는 반려할 수 있습니다.") ShopOrderServiceRequestStatus requestStatus,
    @Schema(description = "신청 일시") LocalDateTime createdAt,
    @Schema(description = "승인 일시") LocalDateTime approvedAt,
    @Schema(description = "반려 사유") String rejectionReason,
    @Schema(description = "승격 데이터가 있는 승인 가능 신청인지 여부") boolean canApprove
) {

    public static AdminShopOrderServiceRequestResponse from(ShopOrderServiceRequest request) {
        return new AdminShopOrderServiceRequestResponse(
            request.getId(), request.getShop().getId(), request.getShop().getName(),
            request.getBank(), request.getAccountNumber(), request.getMinimumOrderAmount(), request.getIsTakeout(),
            request.getDeliveryOption(), request.getDeliveryTip(), request.getBusinessLicenseUrl(),
            request.getBusinessCertificateUrl(), request.getBankCopyUrl(), request.getTransferData(),
            request.getRequestStatus(), request.getCreatedAt(), request.getApprovedAt(), request.getRejectionReason(),
            request.getRequestStatus() == ShopOrderServiceRequestStatus.PENDING
                && request.getTransferData() != null && request.getDeliveryTip() != null
        );
    }
}
