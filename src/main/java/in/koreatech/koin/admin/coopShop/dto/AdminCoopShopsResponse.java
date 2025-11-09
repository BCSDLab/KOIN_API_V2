package in.koreatech.koin.admin.coopShop.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

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

        }

        @JsonNaming(value = SnakeCaseStrategy.class)
        public record InnerOperationHour(
            String type,
            String dayOfWeek,
            String openTime,
            String closeTime
        ) {

        }
    }
}
