package in.koreatech.koin.admin.coopShop.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.admin.coopShop.model.CoopShopRow;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminCoopShopsResponse(
    @Schema(description = "생협 매장 정보 리스트", requiredMode = REQUIRED)
    List<InnerCoopShop> coopShops
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerCoopShop(
        @Schema(description = "생협 매장 기본 정보", requiredMode = REQUIRED)
        InnerCoopShopInfo coopShopInfo,

        @Schema(description = "요일별 생협 매장 운영시간")
        List<InnerOperationHour> operationHours
    ) {

        @JsonNaming(value = SnakeCaseStrategy.class)
        public record InnerCoopShopInfo(
            @Schema(example = "세탁소", description = "생협 매장 이름", requiredMode = REQUIRED)
            String name,

            @Schema(example = "041-560-1234", description = "생협 매장 연락처", requiredMode = REQUIRED)
            String phone,

            @Schema(example = "학생식당 2층", description = "생협 매장 위치", requiredMode = REQUIRED)
            String location,

            @Schema(example = "공휴일 휴무", description = "생협 매장 특이사항")
            String remark
        ) {

            public static InnerCoopShopInfo from(CoopShopRow coopShopRow) {
                return new InnerCoopShopInfo(
                    coopShopRow.coopShopName(),
                    coopShopRow.phone(),
                    coopShopRow.location(),
                    coopShopRow.remark()
                );
            }
        }

        @JsonNaming(value = SnakeCaseStrategy.class)
        public record InnerOperationHour(
            @Schema(example = "아침", description = "생협 매장 운영시간 타입", requiredMode = REQUIRED)
            String type,

            @Schema(example = "평일", description = "생협 매장 운영시간 요일", requiredMode = REQUIRED)
            String dayOfWeek,

            @Schema(example = "09:00", description = "생협 매장 오픈 시간", requiredMode = REQUIRED)
            String openTime,

            @Schema(example = "18:00", description = "생협 매장 마감 시간", requiredMode = REQUIRED)
            String closeTime
        ) {

            public static InnerOperationHour from(CoopShopRow coopShopRow) {
                return new InnerOperationHour(
                    coopShopRow.type(),
                    coopShopRow.dayOfWeek(),
                    coopShopRow.openTime(),
                    coopShopRow.closeTime()
                );
            }
        }

        public static InnerCoopShop from(Map.Entry<InnerCoopShopInfo, List<CoopShopRow>> entry) {
            return new InnerCoopShop(
                entry.getKey(),
                entry.getValue().stream().map(InnerOperationHour::from).toList()
            );
        }
    }
}
