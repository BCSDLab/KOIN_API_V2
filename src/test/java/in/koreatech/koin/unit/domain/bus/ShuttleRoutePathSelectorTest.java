package in.koreatech.koin.unit.domain.bus;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.bus.enums.BusStation;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteType;
import in.koreatech.koin.domain.bus.service.shuttle.internal.ShuttleRoutePathSelector;
import in.koreatech.koin.domain.bus.service.shuttle.internal.ShuttleRoutePathSelector.Occurrence;

class ShuttleRoutePathSelectorTest {

    @Test
    void 종점은_목적지로_인정하지만_그_뒤_정류장과_연결하지_않는다() {
        List<String> nodes = List.of("한기대", "터미널", "천안역", "한기대");
        List<String> times = List.of("15:30", "종점", "16:00", "16:30");

        assertThat(select(nodes, times, ShuttleRouteType.SHUTTLE, null,
            BusStation.KOREATECH, BusStation.TERMINAL)).hasSize(1);
        assertThat(select(nodes, times, ShuttleRouteType.SHUTTLE, null,
            BusStation.KOREATECH, BusStation.STATION)).isEmpty();
        assertThat(select(nodes, times, ShuttleRouteType.SHUTTLE, null,
            BusStation.STATION, BusStation.KOREATECH)).isEmpty();
    }

    @Test
    void 천안역A와B는_정확히_매칭하고_다른_도시_터미널은_매칭하지_않는다() {
        assertThat(select(List.of("천안역A", "한기대"), List.of("08:10", "도착"),
            ShuttleRouteType.WEEKEND, "등교", BusStation.STATION, BusStation.KOREATECH)).hasSize(1);
        assertThat(select(List.of("대전복합터미널", "한기대"), List.of("08:10", "도착"),
            ShuttleRouteType.WEEKEND, "등교", BusStation.TERMINAL, BusStation.KOREATECH)).isEmpty();
    }

    @Test
    void 주중_하교의_역순_저장과_이미_정방향인_저장을_각각_처리한다() {
        assertThat(select(List.of("터미널", "한기대"), List.of("18:50", "18:10"),
            ShuttleRouteType.WEEKDAYS, "하교", BusStation.KOREATECH, BusStation.TERMINAL))
            .containsExactly(new Occurrence(1, 0, java.time.LocalTime.of(18, 10)));
        assertThat(select(List.of("한기대", "터미널"), List.of("19:10", "19:50"),
            ShuttleRouteType.WEEKDAYS, "하교", BusStation.KOREATECH, BusStation.TERMINAL))
            .containsExactly(new Occurrence(0, 1, java.time.LocalTime.of(19, 10)));
    }

    @Test
    void 주중_등교의_역순_저장도_시간이_맞는_경우만_뒤집는다() {
        assertThat(select(List.of("한기대", "터미널"), List.of("08:50", "08:05"),
            ShuttleRouteType.WEEKDAYS, "등교", BusStation.TERMINAL, BusStation.KOREATECH))
            .containsExactly(new Occurrence(1, 0, java.time.LocalTime.of(8, 5)));
    }

    @Test
    void 반복_정류장은_각_실제_구간의_출발을_모두_반환한다() {
        assertThat(select(
            List.of("터미널", "한기대", "터미널", "한기대"),
            List.of("08:00", "도착", "10:00", "도착"),
            ShuttleRouteType.SHUTTLE, null, BusStation.TERMINAL, BusStation.KOREATECH))
            .extracting(Occurrence::departureTime)
            .containsExactly(java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0));
    }

    @Test
    void null과_출발_표식과_24시_시각은_파싱하지_않고_제외한다() {
        assertThat(select(List.of("터미널", "한기대"), Arrays.asList(null, "도착"),
            ShuttleRouteType.SHUTTLE, null, BusStation.TERMINAL, BusStation.KOREATECH)).isEmpty();
        assertThat(select(List.of("터미널", "한기대"), List.of("정차", "도착"),
            ShuttleRouteType.SHUTTLE, null, BusStation.TERMINAL, BusStation.KOREATECH)).isEmpty();
        assertThat(select(List.of("터미널", "한기대"), List.of("24:00", "도착"),
            ShuttleRouteType.SHUTTLE, null, BusStation.TERMINAL, BusStation.KOREATECH)).isEmpty();
    }

    @Test
    void selector는_원본_배열을_변경하지_않는다() {
        List<String> nodes = new ArrayList<>(List.of("터미널", "한기대"));
        List<String> times = new ArrayList<>(List.of("18:50", "18:10"));

        select(nodes, times, ShuttleRouteType.WEEKDAYS, "하교", BusStation.KOREATECH, BusStation.TERMINAL);

        assertThat(nodes).containsExactly("터미널", "한기대");
        assertThat(times).containsExactly("18:50", "18:10");
    }

    private List<Occurrence> select(
        List<String> nodes,
        List<String> times,
        ShuttleRouteType routeType,
        String direction,
        BusStation depart,
        BusStation arrival
    ) {
        return ShuttleRoutePathSelector.select(nodes, times, routeType, direction, depart, arrival);
    }
}
