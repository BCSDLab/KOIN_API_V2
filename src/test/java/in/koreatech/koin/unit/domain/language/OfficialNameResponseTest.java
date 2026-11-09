package in.koreatech.koin.unit.domain.language;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import in.koreatech.koin.domain.bus.controller.BusController;
import in.koreatech.koin.domain.bus.dto.BusScheduleResponse;
import in.koreatech.koin.domain.bus.enums.BusStation;
import in.koreatech.koin.domain.bus.enums.CityBusDirection;
import in.koreatech.koin.domain.bus.service.BusService;
import in.koreatech.koin.domain.bus.service.city.dto.CityBusTimetableResponse;
import in.koreatech.koin.domain.bus.service.shuttle.dto.ShuttleBusTimetableResponse;
import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.language.service.OfficialNameResolver;
import in.koreatech.koin.domain.order.address.controller.AddressController;
import in.koreatech.koin.domain.order.address.dto.CampusDeliveryAddressRequestFilter;
import in.koreatech.koin.domain.order.address.dto.CampusDeliveryAddressResponse;
import in.koreatech.koin.domain.order.address.dto.CampusDeliveryAddressResponse.InnerCampusDeliveryAddressResponse;
import in.koreatech.koin.domain.order.address.service.AddressSearchOpenApiService;
import in.koreatech.koin.domain.order.address.service.CampusAddressSearchService;

class OfficialNameResponseTest {

    @Test
    void 교내주소_표시명만_공식명으로_반환하고_언어별_조회가_원문캐시를_변경하지_않는다() throws Exception {
        var campus = mock(CampusAddressSearchService.class);
        var original = new CampusDeliveryAddressResponse(1, List.of(new InnerCampusDeliveryAddressResponse(
            1, "기숙사", "충남 천안시 생활관 101동", "101동(해울)", "충남 천안시",
            BigDecimal.ONE, BigDecimal.TEN, "101동(해울)")));
        when(campus.getCampusDeliveryAddresses(CampusDeliveryAddressRequestFilter.ALL)).thenReturn(original);
        var mvc = MockMvcBuilders.standaloneSetup(new AddressController(
            mock(AddressSearchOpenApiService.class), campus)).build();

        for (SupportedLanguage language : SupportedLanguage.values()) {
            mvc.perform(get("/address/delivery/campus").param("language", language.getCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addresses[0].display_name")
                    .value(language == SupportedLanguage.ENGLISH ? "Haeul" : "101동(해울)"))
                .andExpect(jsonPath("$.addresses[0].id").value(1))
                .andExpect(jsonPath("$.addresses[0].full_address").value("충남 천안시 생활관 101동"))
                .andExpect(jsonPath("$.addresses[0].short_address").value("101동(해울)"));
        }
        mvc.perform(get("/address/delivery/campus"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.addresses[0].display_name").value("101동(해울)"));
        assertThat(original.addresses().get(0).displayName()).isEqualTo("101동(해울)");
        assertThat(OfficialNameResolver.buildingName("공학 1관", SupportedLanguage.ENGLISH))
            .isEqualTo("Engineering Building 1");
        assertThat(OfficialNameResolver.buildingName("공학 1관 101호", SupportedLanguage.ENGLISH))
            .isEqualTo("공학 1관 101호");
    }

    @Test
    void 버스_응답은_정류장_원문과_식별자를_유지하고_공식표시명만_추가한다() throws Exception {
        var buses = mock(BusService.class);
        var timetable = new ShuttleBusTimetableResponse("route", "천안", "순환", "천안 셔틀", null,
            List.of(new ShuttleBusTimetableResponse.NodeInfoResponse("천안역", "동쪽", "천안역"),
                new ShuttleBusTimetableResponse.NodeInfoResponse("천안역(학화호두과자)", "승차 위치", "천안역(학화호두과자)")),
            List.of(new ShuttleBusTimetableResponse.RouteInfoResponse("1회", "등교", List.of("08:00", "08:10"))));
        when(buses.getShuttleBusTimetable("route")).thenReturn(timetable);
        when(buses.getCityBusTimetable(400L, CityBusDirection.종합터미널))
            .thenReturn(new CityBusTimetableResponse(null,
                CityBusTimetableResponse.BusInfo.of(400L, CityBusDirection.종합터미널), List.of()));
        when(buses.getBusSchedule(any())).thenReturn(new BusScheduleResponse(BusStation.STATION, BusStation.KOREATECH,
            LocalDate.of(2026, 11, 9), LocalTime.NOON, List.of(), "천안역", "한기대"));
        var mvc = MockMvcBuilders.standaloneSetup(new BusController(buses)).build();

        mvc.perform(get("/bus/timetable/shuttle/route").param("language", "en-US"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.node_info[0].name").value("천안역"))
            .andExpect(jsonPath("$.node_info[0].display_name").value("Cheonan Station"))
            .andExpect(jsonPath("$.node_info[1].display_name").value("천안역(학화호두과자)"))
            .andExpect(jsonPath("$.route_info[0].arrival_time[1]").value("08:10"));
        mvc.perform(get("/bus/timetable/shuttle/route").param("language", "ja-JP"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.node_info[0].display_name").value("천안역"));
        mvc.perform(get("/bus/timetable/city").param("bus_number", "400")
                .param("direction", "종합터미널").param("language", "en-US"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.bus_info.depart_node").value("코리아텍"))
            .andExpect(jsonPath("$.bus_info.depart_display_name").value("KOREATECH"))
            .andExpect(jsonPath("$.bus_info.arrival_node").value("천안 터미널"))
            .andExpect(jsonPath("$.bus_info.arrival_display_name").value("Bus Terminal"));
        mvc.perform(get("/bus/route").param("date", "2026-11-09").param("time", "12:00")
                .param("bus_type", "ALL").param("depart", "STATION").param("arrival", "KOREATECH")
                .param("language", "en-US"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.depart").value("STATION"))
            .andExpect(jsonPath("$.arrival").value("KOREATECH"))
            .andExpect(jsonPath("$.depart_display_name").value("Cheonan Station"));
        assertThat(timetable.nodeInfo().get(0).displayName()).isEqualTo("천안역");
    }
}
