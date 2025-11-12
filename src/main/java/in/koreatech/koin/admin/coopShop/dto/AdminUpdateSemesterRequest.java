package in.koreatech.koin.admin.coopShop.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminUpdateSemesterRequest(
    @Schema(description = "학기 ID", requiredMode = REQUIRED)
    @NotNull(message = "학기 ID는 필수 입력값입니다.")
    Integer semesterId,

    @Schema(description = "생협 매장 정보 리스트", requiredMode = REQUIRED)
    @NotNull(message = "생협 매장 정보 리스트는 필수 입력값입니다.")
    List<InnerCoopShop> coopShops
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerCoopShop(
        @Schema(description = "생협 매장 기본 정보", requiredMode = REQUIRED)
        @NotNull(message = "생협 매장 기본 정보는 필수 입력값입니다.")
        InnerCoopShopInfo coopShopInfo,

        @Schema(description = "요일별 생협 매장 운영시간", requiredMode = REQUIRED)
        @NotNull(message = "요일별 생협 매장 운영시간은 필수 입력값입니다.")
        List<InnerOperationHour> operationHours
    ) {

        @JsonNaming(value = SnakeCaseStrategy.class)
        public record InnerCoopShopInfo(
            @Schema(example = "세탁소", description = "생협 매장 이름", requiredMode = REQUIRED)
            @NotBlank(message = "생협 매장 이름은 필수 입력값입니다.")
            String name,

            @Schema(example = "041-560-1234", description = "생협 매장 연락처", requiredMode = REQUIRED)
            @NotBlank(message = "생협 매장 연락처는 필수 입력값입니다.")
            String phone,

            @Schema(example = "학생식당 2층", description = "생협 매장 위치", requiredMode = REQUIRED)
            @NotBlank(message = "생협 매장 위치는 필수 입력값입니다.")
            String location,

            @Schema(example = "공휴일 휴무", description = "생협 매장 특이사항")
            String remark
        ) {

        }

        @JsonNaming(value = SnakeCaseStrategy.class)
        public record InnerOperationHour(
            @Schema(example = "아침", description = "생협 매장 운영시간 타입")
            String type,

            @Schema(example = "평일", description = "생협 매장 운영시간 요일", requiredMode = REQUIRED)
            @NotBlank(message = "생협 매장 운영시간 요일은 필수 입력값입니다.")
            String dayOfWeek,

            @Schema(example = "09:00", description = "생협 매장 오픈 시간", requiredMode = REQUIRED)
            @NotBlank(message = "생협 매장 오픈 시간은 필수 입력값입니다.")
            String openTime,

            @Schema(example = "18:00", description = "생협 매장 마감 시간", requiredMode = REQUIRED)
            @NotBlank(message = "생협 매장 마감 시간은 필수 입력값입니다.")
            String closeTime
        ) {

        }
    }
}
