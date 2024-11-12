package in.koreatech.koin.domain.bus.global.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.bus.shuttle.model.enums.BusStation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record BusScheduleResponse(
    @Schema(description = "출발 정류장", example = "KOREATECH", requiredMode = REQUIRED)
    BusStation depart,
    @Schema(description = "도착 정류장", example = "TERMINAL", requiredMode = REQUIRED)
    BusStation arrival,
    @Schema(description = "출발 날짜", example = "2024-11-05", requiredMode = REQUIRED)
    LocalDate departDate,
    @Schema(description = "출발 시간", example = "12:00", requiredMode = REQUIRED)
    LocalTime departTime,
    @Schema(description = "교통편 조회 결과", example = """
        [
            {
            	"bus_type" : "express",
            	"route_name" : "대성티앤이",
            	"depart_time" : "16:50"
            },
            {
            	"bus_type" : "city",
            	"route_name" : "400",
            	"depart_time" : "16:56"
            },
            {
            	"bus_type" : "city",
            	"route_name" : "402",
            	"depart_time" : "17:30"
            },
            {
            	"bus_type" : "shuttle",
            	"route_name" : "주중(20시 00분)",
            	"depart_time" : "20:00"
            }
        ]
        """, requiredMode = NOT_REQUIRED)
    List<ScheduleInfo> schedule

){
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record ScheduleInfo(
        String busType,
        String busName,
        LocalTime departTime
    ) {
        public static List<ScheduleInfo> toScheduleInfo(List<LocalTime> schedule, String busType, String busName){
            return schedule.stream()
                .map(time -> new BusScheduleResponse.ScheduleInfo(busType, busName, time))
                .collect(Collectors.toList());
        }
    }
}
