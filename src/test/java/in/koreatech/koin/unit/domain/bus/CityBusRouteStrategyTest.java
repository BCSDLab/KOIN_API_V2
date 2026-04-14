package in.koreatech.koin.unit.domain.bus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.domain.bus.dto.BusRouteCommand;
import in.koreatech.koin.domain.bus.dto.BusScheduleResponse.ScheduleInfo;
import in.koreatech.koin.domain.bus.enums.BusRouteType;
import in.koreatech.koin.domain.bus.enums.BusStation;
import in.koreatech.koin.domain.bus.service.city.model.CityBusTimetable;
import in.koreatech.koin.domain.bus.service.city.repository.CityBusTimetableRepository;
import in.koreatech.koin.domain.bus.service.model.route.CityBusRouteStrategy;

@ExtendWith(MockitoExtension.class)
class CityBusRouteStrategyTest {

    @InjectMocks
    private CityBusRouteStrategy cityBusRouteStrategy;

    @Mock
    private CityBusTimetableRepository cityBusTimetableRepository;

    // 평일 날짜 고정
    private static final LocalDate WEEKDAY = LocalDate.of(2024, 4, 8); // 월요일
    private static final LocalTime QUERY_TIME = LocalTime.of(10, 0);

    private CityBusTimetable timetable400Terminal;
    private CityBusTimetable timetable402Terminal;
    private CityBusTimetable timetable405Terminal;
    private CityBusTimetable timetable400Koreatech;
    private CityBusTimetable timetable402Koreatech;
    private CityBusTimetable timetable405Koreatech;

    @BeforeEach
    void setUp() {
        CityBusTimetable.BusTimetable weekdayTimetable = CityBusTimetable.BusTimetable.builder()
            .dayOfWeek("평일")
            .departInfo(List.of("09:00", "11:00", "13:00"))
            .build();

        // 종합터미널 출발 (TERMINAL, STATION 방면 조회 시 사용)
        timetable400Terminal = CityBusTimetable.builder()
            .busInfo(CityBusTimetable.BusInfo.builder().number(400L).depart("종합터미널").arrival("코리아텍").build())
            .busTimetables(List.of(weekdayTimetable))
            .updatedAt(LocalDateTime.now())
            .build();

        timetable402Terminal = CityBusTimetable.builder()
            .busInfo(CityBusTimetable.BusInfo.builder().number(402L).depart("종합터미널").arrival("코리아텍").build())
            .busTimetables(List.of(weekdayTimetable))
            .updatedAt(LocalDateTime.now())
            .build();

        timetable405Terminal = CityBusTimetable.builder()
            .busInfo(CityBusTimetable.BusInfo.builder().number(405L).depart("종합터미널").arrival("코리아텍").build())
            .busTimetables(List.of(weekdayTimetable))
            .updatedAt(LocalDateTime.now())
            .build();

        // 코리아텍 출발 (KOREATECH 방면 조회 시 사용)
        timetable400Koreatech = CityBusTimetable.builder()
            .busInfo(CityBusTimetable.BusInfo.builder().number(400L).depart("병천3리").arrival("종합터미널").build())
            .busTimetables(List.of(weekdayTimetable))
            .updatedAt(LocalDateTime.now())
            .build();

        timetable402Koreatech = CityBusTimetable.builder()
            .busInfo(CityBusTimetable.BusInfo.builder().number(402L).depart("황사동").arrival("종합터미널").build())
            .busTimetables(List.of(weekdayTimetable))
            .updatedAt(LocalDateTime.now())
            .build();

        timetable405Koreatech = CityBusTimetable.builder()
            .busInfo(CityBusTimetable.BusInfo.builder().number(405L).depart("유관순열사사적지").arrival("종합터미널").build())
            .busTimetables(List.of(weekdayTimetable))
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Nested
    @DisplayName("학교 방면 시내버스 시간표 조회")
    class SchoolDirectionTest {

        @Test
        @DisplayName("터미널 → 천안역: 종합터미널 기점 시간표를 반환한다")
        void 터미널에서_천안역_시간표_조회() {
            // given
            BusRouteCommand command = new BusRouteCommand(
                BusStation.TERMINAL, BusStation.STATION, BusRouteType.CITY, WEEKDAY, QUERY_TIME);

            when(cityBusTimetableRepository.getByBusInfoNumberAndBusInfoDepart(400L, "종합터미널"))
                .thenReturn(timetable400Terminal);
            when(cityBusTimetableRepository.getByBusInfoNumberAndBusInfoDepart(402L, "종합터미널"))
                .thenReturn(timetable402Terminal);
            when(cityBusTimetableRepository.getByBusInfoNumberAndBusInfoDepart(405L, "종합터미널"))
                .thenReturn(timetable405Terminal);

            // when
            List<ScheduleInfo> result = cityBusRouteStrategy.findSchedule(command);

            // then
            assertThat(result).isNotEmpty();
            assertThat(result).hasSize(9); // 3개 버스 × 3개 시간
            assertThat(result).allMatch(info -> info.busType().equals("city"));
        }

        @Test
        @DisplayName("터미널 → 코리아텍: 종합터미널 기점 시간표를 반환한다")
        void 터미널에서_코리아텍_시간표_조회() {
            // given
            BusRouteCommand command = new BusRouteCommand(
                BusStation.TERMINAL, BusStation.KOREATECH, BusRouteType.CITY, WEEKDAY, QUERY_TIME);

            when(cityBusTimetableRepository.getByBusInfoNumberAndBusInfoDepart(400L, "종합터미널"))
                .thenReturn(timetable400Terminal);
            when(cityBusTimetableRepository.getByBusInfoNumberAndBusInfoDepart(402L, "종합터미널"))
                .thenReturn(timetable402Terminal);
            when(cityBusTimetableRepository.getByBusInfoNumberAndBusInfoDepart(405L, "종합터미널"))
                .thenReturn(timetable405Terminal);

            // when
            List<ScheduleInfo> result = cityBusRouteStrategy.findSchedule(command);

            // then
            assertThat(result).isNotEmpty();
            assertThat(result).hasSize(9);
            assertThat(result).allMatch(info -> info.busType().equals("city"));
        }

        @Test
        @DisplayName("천안역 → 코리아텍: 종합터미널 기점 시간표를 반환한다 (천안역 도착 시간 보정 적용)")
        void 천안역에서_코리아텍_시간표_조회() {
            // given
            BusRouteCommand command = new BusRouteCommand(
                BusStation.STATION, BusStation.KOREATECH, BusRouteType.CITY, WEEKDAY, QUERY_TIME);

            when(cityBusTimetableRepository.getByBusInfoNumberAndBusInfoDepart(400L, "종합터미널"))
                .thenReturn(timetable400Terminal);
            when(cityBusTimetableRepository.getByBusInfoNumberAndBusInfoDepart(402L, "종합터미널"))
                .thenReturn(timetable402Terminal);
            when(cityBusTimetableRepository.getByBusInfoNumberAndBusInfoDepart(405L, "종합터미널"))
                .thenReturn(timetable405Terminal);

            // when
            List<ScheduleInfo> result = cityBusRouteStrategy.findSchedule(command);

            // then
            assertThat(result).isNotEmpty();
            assertThat(result).hasSize(9);
            // 천안역 출발 시 +7분 보정이 적용된 시간
            assertThat(result).anyMatch(info -> info.departTime().equals(LocalTime.of(9, 7)));
        }
    }

    @Nested
    @DisplayName("터미널 방면 시내버스 시간표 조회")
    class TerminalDirectionTest {

        @Test
        @DisplayName("천안역 → 터미널: 응답하지 않고 빈 목록을 반환한다")
        void 천안역에서_터미널_시간표_조회_빈목록_반환() {
            // given
            BusRouteCommand command = new BusRouteCommand(
                BusStation.STATION, BusStation.TERMINAL, BusRouteType.CITY, WEEKDAY, QUERY_TIME);

            // when
            List<ScheduleInfo> result = cityBusRouteStrategy.findSchedule(command);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("코리아텍 → 터미널: 각 버스 방면(병천3리/황사동/유관순열사사적지) 기점 시간표를 반환한다")
        void 코리아텍에서_터미널_시간표_조회() {
            // given
            BusRouteCommand command = new BusRouteCommand(
                BusStation.KOREATECH, BusStation.TERMINAL, BusRouteType.CITY, WEEKDAY, QUERY_TIME);

            when(cityBusTimetableRepository.getByBusInfoNumberAndBusInfoDepart(400L, "병천3리"))
                .thenReturn(timetable400Koreatech);
            when(cityBusTimetableRepository.getByBusInfoNumberAndBusInfoDepart(402L, "황사동"))
                .thenReturn(timetable402Koreatech);
            when(cityBusTimetableRepository.getByBusInfoNumberAndBusInfoDepart(405L, "유관순열사사적지"))
                .thenReturn(timetable405Koreatech);

            // when
            List<ScheduleInfo> result = cityBusRouteStrategy.findSchedule(command);

            // then
            assertThat(result).isNotEmpty();
            assertThat(result).hasSize(9);
            // 코리아텍 출발 시 버스별 보정 시간 적용 (400: +6분, 402: +13분, 405: +7분)
            assertThat(result).anyMatch(info ->
                info.busName().equals("400") && info.departTime().equals(LocalTime.of(9, 6)));
            assertThat(result).anyMatch(info ->
                info.busName().equals("402") && info.departTime().equals(LocalTime.of(9, 13)));
            assertThat(result).anyMatch(info ->
                info.busName().equals("405") && info.departTime().equals(LocalTime.of(9, 7)));
        }

        @Test
        @DisplayName("코리아텍 → 천안역: 각 버스 방면(병천3리/황사동/유관순열사사적지) 기점 시간표를 반환한다")
        void 코리아텍에서_천안역_시간표_조회() {
            // given
            BusRouteCommand command = new BusRouteCommand(
                BusStation.KOREATECH, BusStation.STATION, BusRouteType.CITY, WEEKDAY, QUERY_TIME);

            when(cityBusTimetableRepository.getByBusInfoNumberAndBusInfoDepart(400L, "병천3리"))
                .thenReturn(timetable400Koreatech);
            when(cityBusTimetableRepository.getByBusInfoNumberAndBusInfoDepart(402L, "황사동"))
                .thenReturn(timetable402Koreatech);
            when(cityBusTimetableRepository.getByBusInfoNumberAndBusInfoDepart(405L, "유관순열사사적지"))
                .thenReturn(timetable405Koreatech);

            // when
            List<ScheduleInfo> result = cityBusRouteStrategy.findSchedule(command);

            // then
            assertThat(result).isNotEmpty();
            assertThat(result).hasSize(9);
            assertThat(result).allMatch(info -> info.busType().equals("city"));
        }
    }
}
