package in.koreatech.koin.unit.domain.bus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

import in.koreatech.koin.domain.bus.dto.BusRemainTimeResponse;
import in.koreatech.koin.domain.bus.dto.BusRemainTimeResponse.InnerBusResponse;
import in.koreatech.koin.domain.bus.enums.BusStation;
import in.koreatech.koin.domain.bus.enums.BusType;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteType;
import in.koreatech.koin.domain.bus.service.BusNoticeRepository;
import in.koreatech.koin.domain.bus.service.BusService;
import in.koreatech.koin.domain.bus.service.city.CityBusService;
import in.koreatech.koin.domain.bus.service.express.ExpressBusService;
import in.koreatech.koin.domain.bus.service.shuttle.ShuttleBusRepository;
import in.koreatech.koin.domain.bus.service.shuttle.ShuttleBusService;
import in.koreatech.koin.domain.bus.service.shuttle.model.ArrivalNode;
import in.koreatech.koin.domain.bus.service.shuttle.model.Route;
import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.model.VersionType;
import in.koreatech.koin.domain.version.service.VersionService;

@ExtendWith(MockitoExtension.class)
class ShuttleBusRemainTimeTest {

    private static final String SEMESTER = "정규학기";
    private static final Clock CLOCK = Clock.fixed(
        Instant.parse("2026-09-05T02:25:00Z"), ZoneId.of("Asia/Seoul"));

    @Mock
    private ShuttleBusRepository shuttleBusRepository;

    @Mock
    private VersionService versionService;

    @Mock
    private BusNoticeRepository busNoticeRepository;

    @Mock
    private ExpressBusService expressBusService;

    @Mock
    private CityBusService cityBusService;

    private BusService busService;

    @BeforeEach
    void setUp() {
        when(versionService.getVersionEntity(VersionType.SHUTTLE))
            .thenReturn(Version.builder().title(SEMESTER).build());
        ShuttleBusService shuttleBusService = new ShuttleBusService(versionService, shuttleBusRepository, CLOCK);
        busService = new BusService(
            CLOCK, busNoticeRepository, versionService, List.of(), expressBusService, cityBusService, shuttleBusService);
    }

    @Test
    void 토요일_순환버스와_중복_정류장이_있는_주말버스를_함께_조회한다() {
        // 공개 시간표의 토요일 오후 순환 노선과 하교 3회 구조를 재현한다.
        Route cycle = route(ShuttleRouteType.SHUTTLE, null,
            node("한기대", "14:00"), node("2캠퍼스", null), node("터미널", "14:25"),
            node("천안역", "14:30"), node("한기대", "15:00"));
        Route weekend = route(ShuttleRouteType.WEEKEND, "하교",
            node("한기대", "19:10"), node("천안역B", "19:40"), node("터미널", "19:50"),
            node("두정역", "하차"), node("터미널", null), node("천안역A", null), node("한기대", null));
        when(shuttleBusRepository.findAllBySemesterTypeAndRouteType(SEMESTER, ShuttleRouteType.SHUTTLE))
            .thenReturn(List.of(cycle));
        when(shuttleBusRepository.findAllBySemesterTypeAndRouteType(SEMESTER, ShuttleRouteType.WEEKEND))
            .thenReturn(List.of(weekend));

        BusRemainTimeResponse response = getRemainTime(BusType.SHUTTLE);

        assertThat(response).isEqualTo(new BusRemainTimeResponse(
            "shuttle", new InnerBusResponse(null, 10800L), null));
    }

    @ParameterizedTest
    @EnumSource(value = BusType.class, names = {"SHUTTLE", "COMMUTING"})
    void 무효_시간을_제외하고_중복없는_가까운_두_버스를_반환한다(BusType busType) {
        ShuttleRouteType type = routeType(busType);
        givenRoutes(busType, List.of(
            departure(type, "15:00"),
            duplicateDeparture(type, null),
            departure(type, "14:25"),
            duplicateDeparture(type, "정차"),
            departure(type, "14:25"),
            departure(type, "16:00"),
            departure(type, "10:00")
        ));

        BusRemainTimeResponse response = getRemainTime(busType);

        assertThat(response).isEqualTo(new BusRemainTimeResponse(
            busType.getName(), new InnerBusResponse(null, 10800L), new InnerBusResponse(null, 12900L)));
    }

    @ParameterizedTest
    @EnumSource(value = BusType.class, names = {"SHUTTLE", "COMMUTING"})
    void 계산할_수_없는_시간과_운행_종료_항목만_있으면_빈_응답을_반환한다(BusType busType) {
        ShuttleRouteType type = routeType(busType);
        givenRoutes(busType, List.of(
            duplicateDeparture(type, null), duplicateDeparture(type, "정차"), departure(type, "10:00")));

        assertThat(getRemainTime(busType)).isEqualTo(new BusRemainTimeResponse(busType.getName(), null, null));
    }

    @ParameterizedTest
    @EnumSource(value = BusType.class, names = {"SHUTTLE", "COMMUTING"})
    void 노선이_없으면_빈_응답을_반환한다(BusType busType) {
        givenRoutes(busType, List.of());

        assertThat(getRemainTime(busType)).isEqualTo(new BusRemainTimeResponse(busType.getName(), null, null));
    }

    private BusRemainTimeResponse getRemainTime(BusType busType) {
        return busService.getBusRemainTime(busType, BusStation.TERMINAL, BusStation.KOREATECH);
    }

    private void givenRoutes(BusType busType, List<Route> routes) {
        when(shuttleBusRepository.findAllBySemesterTypeAndRouteType(SEMESTER, routeType(busType)))
            .thenReturn(routes);
    }

    private ShuttleRouteType routeType(BusType busType) {
        return busType == BusType.SHUTTLE ? ShuttleRouteType.SHUTTLE : ShuttleRouteType.WEEKDAYS;
    }

    private Route departure(ShuttleRouteType type, String time) {
        return route(type, "등교", node("터미널", time), node("한기대", "도착"));
    }

    private Route duplicateDeparture(ShuttleRouteType type, String firstTime) {
        // 노선 판정과 시각 추출의 선택 불일치로 null 시각 객체가 생기는 경계를 재현한다.
        return route(type, "등교", node("터미널", firstTime), node("터미널", "19:50"), node("한기대", "도착"));
    }

    private Route route(ShuttleRouteType type, String direction, ArrivalNode... nodes) {
        Route route = BeanUtils.instantiateClass(Route.class);
        ReflectionTestUtils.setField(route, "routeName", "테스트 노선");
        ReflectionTestUtils.setField(route, "routeType", type);
        ReflectionTestUtils.setField(route, "routeInfo", direction);
        ReflectionTestUtils.setField(route, "routeDetail", direction);
        ReflectionTestUtils.setField(route, "runningDays", List.of("SAT"));
        ReflectionTestUtils.setField(route, "arrivalNodes", new ArrayList<>(List.of(nodes)));
        return route;
    }

    private ArrivalNode node(String name, String time) {
        ArrivalNode node = BeanUtils.instantiateClass(ArrivalNode.class);
        ReflectionTestUtils.setField(node, "nodeName", name);
        ReflectionTestUtils.setField(node, "arrivalTime", time);
        return node;
    }
}
