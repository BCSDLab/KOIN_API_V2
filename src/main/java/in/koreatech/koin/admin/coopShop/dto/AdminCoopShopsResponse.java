package in.koreatech.koin.admin.coopShop.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.admin.coopShop.model.CoopShopRow;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminCoopShopsResponse(
    List<InnerCoopShop> coopShops
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerCoopShop(
        InnerCoopShopInfo coopShopInfo,
        List<InnerOperationHour> operationHours
    ) {

        @JsonNaming(value = SnakeCaseStrategy.class)
        public record InnerCoopShopInfo(
            String name,
            String phone,
            String location,
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
            String type,
            String dayOfWeek,
            String openTime,
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
