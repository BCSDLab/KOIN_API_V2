package in.koreatech.koin.admin.ownershop.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.common.model.Criteria;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequest;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestDeliveryOption;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AdminShopOrderServiceResponse(
    @Schema(description = "조건에 해당하는 총 주문 서비스 요청의 수", example = "57", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "조건에 해당하는 주문 서비스 요청 중에 현재 페이지에서 조회된 수", example = "10", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "조건에 해당하는 주문 서비스 요청들을 조회할 수 있는 최대 페이지", example = "6", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "2", requiredMode = REQUIRED)
    Integer currentPage,

    @Schema(description = "주문 서비스 요청 리스트", requiredMode = REQUIRED)
    List<InnerShopOrderServiceResponse> orderServiceRequests
) {
    @JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record InnerShopOrderServiceResponse(
        @Schema(description = "고유 id", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "상점 id", requiredMode = REQUIRED)
        Integer shopId,

        @Schema(description = "상점명", requiredMode = REQUIRED)
        String shopName,

        // @Schema(description = "최소 주문 금액", example = "10000", requiredMode = REQUIRED)
        // Integer minimumOrderAmount,

        // @Schema(description = "포장 가능 여부", example = "true", requiredMode = REQUIRED)
        // Boolean isTakeout,
        //
        // @Schema(description = "배달 옵션", example = "CAMPUS", requiredMode = REQUIRED)
        // ShopOrderServiceRequestDeliveryOption deliveryOption,
        //
        // @Schema(description = "교내 배달비", example = "2000", requiredMode = REQUIRED)
        // Integer campusDeliveryTip,
        //
        // @Schema(description = "교외 배달비", example = "3000", requiredMode = REQUIRED)
        // Integer offCampusDeliveryTip,

        @Schema(description = "요청 상태", example = "PENDING", requiredMode = REQUIRED)
        ShopOrderServiceRequestStatus requestStatus,

        // @Schema(description = "사업자등록증 URL", requiredMode = REQUIRED)
        // String businessLicenseUrl,
        //
        // @Schema(description = "사업자등록 증명서 URL", requiredMode = REQUIRED)
        // String businessCertificateUrl,
        //
        // @Schema(description = "통장 사본 URL", requiredMode = REQUIRED)
        // String bankCopyUrl,
        //
        // @Schema(description = "은행명", example = "국민은행", requiredMode = REQUIRED)
        // String bank,
        //
        // @Schema(description = "계좌번호", example = "123-456-789", requiredMode = REQUIRED)
        // String accountNumber,

        @Schema(description = "신청 일자", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,

        @Schema(description = "승인 일자", requiredMode = NOT_REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime approvedAt
    ) {
        public static InnerShopOrderServiceResponse from(ShopOrderServiceRequest shopOrderServiceRequest) {
            return new InnerShopOrderServiceResponse(
                shopOrderServiceRequest.getId(),
                shopOrderServiceRequest.getShop().getId(),
                shopOrderServiceRequest.getShop().getName(),
                // shopOrderServiceRequest.getMinimumOrderAmount(),
                // shopOrderServiceRequest.getIsTakeout(),
                // shopOrderServiceRequest.getDeliveryOption(),
                // shopOrderServiceRequest.getCampusDeliveryTip(),
                // shopOrderServiceRequest.getOffCampusDeliveryTip(),
                shopOrderServiceRequest.getRequestStatus(),
                // shopOrderServiceRequest.getBusinessLicenseUrl(),
                // shopOrderServiceRequest.getBusinessCertificateUrl(),
                // shopOrderServiceRequest.getBankCopyUrl(),
                // shopOrderServiceRequest.getBank(),
                // shopOrderServiceRequest.getAccountNumber(),
                shopOrderServiceRequest.getCreatedAt(),
                shopOrderServiceRequest.getApprovedAt()
            );
        }
    }

    public static AdminShopOrderServiceResponse of(Page<ShopOrderServiceRequest> pagedResult, Criteria criteria) {
        return new AdminShopOrderServiceResponse(
            pagedResult.getTotalElements(),
            pagedResult.getContent().size(),
            pagedResult.getTotalPages(),
            criteria.getPage() + 1,
            pagedResult.getContent().stream()
                .map(InnerShopOrderServiceResponse::from)
                .collect(Collectors.toList())
        );
    }
}
