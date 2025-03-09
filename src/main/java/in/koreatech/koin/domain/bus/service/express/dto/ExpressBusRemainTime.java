package in.koreatech.koin.domain.bus.service.express.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalTime;

import in.koreatech.koin.domain.bus.service.model.BusRemainTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class ExpressBusRemainTime extends BusRemainTime {

    @Schema(description = "버스 타입", example = "express", requiredMode = REQUIRED)
    private final String busType;

    public ExpressBusRemainTime(LocalTime busArrivalTime, String busType) {
        super(busArrivalTime, null);
        this.busType = busType;
    }
}
