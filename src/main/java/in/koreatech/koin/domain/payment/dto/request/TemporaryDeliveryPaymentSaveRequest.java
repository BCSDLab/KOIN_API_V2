package in.koreatech.koin.domain.payment.dto.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.delivery.model.AddressType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@JsonNaming(value = SnakeCaseStrategy.class)
public record TemporaryDeliveryPaymentSaveRequest(
    @Schema(description = "배달 주소", example = "충청남도 천안시 동남구 병천면 충절로 1600", requiredMode = REQUIRED)
    @NotBlank(message = "배달 주소는 필수 입력사항입니다.")
    String address,

    @Schema(description = "배달 상세 주소", example = "은솔관 422호", requiredMode = NOT_REQUIRED)
    String addressDetail,

    @Schema(description = "배달 주소 위도", example = "36.76125794", requiredMode = REQUIRED)
    @NotNull(message = "배달 주소 위도는 필수 입력사항입니다.")
    BigDecimal longitude,

    @Schema(description = "배달 주소 경도", example = "127.28372942", requiredMode = REQUIRED)
    @NotNull(message = "배달 주소 경도는 필수 입력사항입니다.")
    BigDecimal latitude,

    @Schema(description = "연락처", example = "01012345678", requiredMode = REQUIRED)
    @NotBlank(message = "연락처는 필수 입력사항입니다.")
    @Pattern(regexp = "^\\d{11}$", message = "전화번호 형식이 올바르지 않습니다. 11자리 숫자로 입력해 주세요.")
    String phoneNumber,

    @Schema(description = "사장님에게", example = "리뷰 이벤트 감사합니다.", requiredMode = NOT_REQUIRED)
    String toOwner,

    @Schema(description = "라이더에게", example = "문 앞에 놔주세요.", requiredMode = NOT_REQUIRED)
    String toRider,

    @Schema(description = "메뉴 총 금액", example = "1234", requiredMode = REQUIRED)
    @NotNull(message = "메뉴 총 금액은 필수 입력사항입니다.")
    Integer totalMenuPrice,

    @Schema(description = "배달 타입", example = "OFF_CAMPUS", requiredMode = REQUIRED)
    @NotNull(message = "배달 타입은 필수 입력사항입니다.")
    AddressType deliveryType,

    @Schema(description = "배달 팁", example = "1234", requiredMode = REQUIRED)
    @NotNull(message = "배달 팁은 필수 입력사항입니다.")
    Integer deliveryTip,

    @Schema(description = "수저, 포크 수령 여부", example = "true", requiredMode = REQUIRED)
    @NotNull(message = "수저, 포크 수령 여부는 필수 입력사항입니다.")
    Boolean provideCutlery,

    @Schema(description = "결제 금액", example = "10000", requiredMode = REQUIRED)
    @NotNull(message = "결제 금액은 필수 입력사항입니다.")
    Integer totalAmount
) {

}
