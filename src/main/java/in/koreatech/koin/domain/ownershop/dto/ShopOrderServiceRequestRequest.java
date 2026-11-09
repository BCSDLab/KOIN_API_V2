package in.koreatech.koin.domain.ownershop.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestDeliveryOption;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
public record ShopOrderServiceRequestRequest(

    @Schema(description = "최소 주문 금액", example = "5000", requiredMode = REQUIRED)
    @NotNull(message = "최소 주문 금액은 필수입니다.")
    @Min(value = 0, message = "최소 주문 금액은 0원 이상이어야 합니다.")
    Integer minimumOrderAmount,

    @Schema(description = "포장 가능 여부", example = "true", requiredMode = REQUIRED)
    @NotNull(message = "포장 가능 여부는 필수입니다.")
    Boolean isTakeout,

    @Schema(description = "배달 옵션 (CAMPUS/OFF_CAMPUS/BOTH)", example = "BOTH", requiredMode = REQUIRED)
    @NotNull(message = "배달 옵션은 필수입니다.")
    ShopOrderServiceRequestDeliveryOption deliveryOption,

    @Schema(description = "교내 배달팁", example = "1000", requiredMode = REQUIRED)
    @NotNull(message = "교내 배달팁은 필수입니다.")
    @Min(value = 0, message = "교내 배달팁은 0원 이상이어야 합니다.")
    Integer campusDeliveryTip,

    @Schema(description = "교외 배달팁", example = "2000", requiredMode = REQUIRED)
    @NotNull(message = "교외 배달팁은 필수입니다.")
    @Min(value = 0, message = "교외 배달팁은 0원 이상이어야 합니다.")
    Integer offCampusDeliveryTip,

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
    String accountNumber,

    @Schema(description = "예금주", example = "홍길동", requiredMode = REQUIRED)
    @NotBlank(message = "예금주는 필수입니다.")
    @Size(max = 50, message = "예금주는 50자 이하여야 합니다.")
    String accountHolder,

    @Schema(description = "이관할 메뉴와 메뉴별 추가 옵션 그룹", requiredMode = REQUIRED)
    @NotEmpty(message = "이관할 메뉴를 하나 이상 선택해야 합니다.")
    List<@NotNull @Valid MenuSelection> menus
) {

    @JsonIgnore
    @AssertTrue(message = "이관할 메뉴 ID는 중복될 수 없습니다.")
    public boolean isMenuIdsUnique() {
        if (menus == null) {
            return true;
        }
        List<Integer> menuIds = menus.stream()
            .filter(Objects::nonNull)
            .map(MenuSelection::menuId)
            .filter(Objects::nonNull)
            .toList();
        return menuIds.stream().distinct().count() == menuIds.size();
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record MenuSelection(
        @Schema(description = "이관할 기존 메뉴 ID", example = "101", requiredMode = REQUIRED)
        @NotNull(message = "메뉴 ID는 필수입니다.")
        @Min(value = 1, message = "메뉴 ID는 1 이상이어야 합니다.")
        Integer menuId,

        @Schema(description = "메뉴에 추가할 옵션 그룹 목록")
        List<@NotNull @Valid OptionGroup> optionGroups
    ) {

        public MenuSelection {
            if (optionGroups == null) {
                optionGroups = List.of();
            }
        }
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record OptionGroup(
        @Schema(description = "옵션 그룹명", example = "맵기 선택", requiredMode = REQUIRED)
        @NotBlank(message = "옵션 그룹명은 필수입니다.")
        @Size(max = 255, message = "옵션 그룹명은 255자 이하여야 합니다.")
        String name,

        @Schema(description = "옵션 그룹 설명", example = "맵기를 선택해주세요.")
        String description,

        @Schema(description = "필수 선택 여부", example = "true", requiredMode = REQUIRED)
        @NotNull(message = "옵션 그룹의 필수 선택 여부는 필수입니다.")
        Boolean isRequired,

        @Schema(description = "최소 선택 개수", example = "1", requiredMode = REQUIRED)
        @NotNull(message = "최소 선택 개수는 필수입니다.")
        @Min(value = 0, message = "최소 선택 개수는 0 이상이어야 합니다.")
        Integer minSelect,

        @Schema(description = "최대 선택 개수", example = "1", requiredMode = REQUIRED)
        @NotNull(message = "최대 선택 개수는 필수입니다.")
        @Min(value = 1, message = "최대 선택 개수는 1 이상이어야 합니다.")
        Integer maxSelect,

        @Schema(description = "추가 옵션 목록", requiredMode = REQUIRED)
        @NotEmpty(message = "옵션 그룹에는 옵션이 하나 이상 있어야 합니다.")
        List<@NotNull @Valid Option> options
    ) {

        @JsonIgnore
        @AssertTrue(message = "최소·최대 선택 개수는 옵션 목록과 일치해야 합니다.")
        public boolean isSelectionRangeValid() {
            if (minSelect == null || maxSelect == null || options == null) {
                return true;
            }
            return minSelect <= maxSelect && maxSelect <= options.size();
        }

        @JsonIgnore
        @AssertTrue(message = "최소 선택 개수가 1 이상인 그룹만 필수로 설정할 수 있습니다.")
        public boolean isRequiredSelectionValid() {
            if (isRequired == null || minSelect == null) {
                return true;
            }
            return isRequired == (minSelect > 0);
        }
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record Option(
        @Schema(description = "옵션명", example = "치즈 추가", requiredMode = REQUIRED)
        @NotBlank(message = "옵션명은 필수입니다.")
        @Size(max = 255, message = "옵션명은 255자 이하여야 합니다.")
        String name,

        @Schema(description = "옵션 추가 가격", example = "1000", requiredMode = REQUIRED)
        @NotNull(message = "옵션 추가 가격은 필수입니다.")
        @Min(value = 0, message = "옵션 추가 가격은 0원 이상이어야 합니다.")
        Integer price
    ) {
    }
}
