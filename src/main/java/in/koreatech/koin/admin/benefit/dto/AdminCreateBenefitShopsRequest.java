package in.koreatech.koin.admin.benefit.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.global.validation.NotBlankElement;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminCreateBenefitShopsRequest(
    @Schema(
        description = "상점정보 리스트",
        example = "[{\"shop_id\": 1, \"detail\": \"배달비 무료\"}, {\"shop_id\": 2, \"detail\": \"최소주문금액 0원\"}]",
        requiredMode = REQUIRED
    )
    @NotNull(message = "상점정보 리스트는 필수입니다.")
    @NotBlankElement(message = "상점정보 리스트는 빈 요소가 존재할 수 없습니다.")
    List<InnerBenefitShopsRequest> shopDetails
) {

    @JsonNaming(SnakeCaseStrategy.class)
    public record InnerBenefitShopsRequest(
        @Schema(description = "상점 고유 id", example = "2", requiredMode = REQUIRED)
        @NotNull(message = "상점은 필수입니다.")
        Integer shopId,

        @Schema(description = "혜택 미리보기 문구", example = "4인 이상 픽업서비스", requiredMode = REQUIRED)
        @NotBlank(message = "혜택 미리보기 문구는 필수입니다.")
        @Size(min = 2, max = 15, message = "혜택 미리보기 문구는 최소 2자 최대 15자입니다.")
        String detail
    ) {
    }
}
