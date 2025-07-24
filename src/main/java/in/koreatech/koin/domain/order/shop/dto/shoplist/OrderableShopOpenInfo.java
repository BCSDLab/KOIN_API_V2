package in.koreatech.koin.domain.order.shop.dto.shoplist;

import java.time.DayOfWeek;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(value = SnakeCaseStrategy.class)
public record OrderableShopOpenInfo(
    @JsonIgnore
    Integer shopId,
    String dayOfWeek,
    Boolean closed,
    LocalTime openTime,
    LocalTime closeTime
) {

    public Boolean isScheduledToOpenAt(DayOfWeek currentDayOfWeek, LocalTime currentTime) {
        if (this.closed() || this.openTime == null || this.closeTime == null) {
            return false;
        }

        DayOfWeek scheduledDay = DayOfWeek.valueOf(this.dayOfWeek().toUpperCase());

        return scheduledDay.equals(currentDayOfWeek) &&
            !currentTime.isBefore(this.openTime()) &&
            currentTime.isBefore(this.closeTime());
    }
}
