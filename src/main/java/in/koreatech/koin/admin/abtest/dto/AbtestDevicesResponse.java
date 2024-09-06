package in.koreatech.koin.admin.abtest.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.admin.abtest.model.Device;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record AbtestDevicesResponse(
    List<InnerDeviceResponse> devices
) {

    public static AbtestDevicesResponse from(List<Device> devices) {
        return new AbtestDevicesResponse(devices.stream().map(InnerDeviceResponse::from).toList());
    }

    @JsonNaming(SnakeCaseStrategy.class)
    private record InnerDeviceResponse(
        @Schema(description = "디바이스 ID", example = "1")
        Integer id,

        @Schema(description = "디바이스 타입", example = "mobile")
        String type,

        @Schema(description = "디바이스 모델", example = "Galaxy20")
        String model,

        @Schema(description = "마지막 접속 날짜", example = "2019-07-29", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate lastAccessedAt
    ) {

        public static InnerDeviceResponse from(Device device) {
            return new InnerDeviceResponse(
                device.getId(),
                device.getType(),
                device.getModel(),
                device.getAccessHistory().getLastAccessedAt().toLocalDate());
        }
    }
}
