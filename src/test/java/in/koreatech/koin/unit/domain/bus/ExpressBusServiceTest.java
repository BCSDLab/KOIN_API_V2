package in.koreatech.koin.unit.domain.bus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.domain.bus.dto.SingleBusTimeResponse;
import in.koreatech.koin.domain.bus.enums.BusStation;
import in.koreatech.koin.domain.bus.enums.BusType;
import in.koreatech.koin.domain.bus.service.express.ExpressBusCacheRepository;
import in.koreatech.koin.domain.bus.service.express.ExpressBusService;
import in.koreatech.koin.domain.bus.service.express.dto.ExpressBusRemainTime;
import in.koreatech.koin.domain.bus.service.express.model.ExpressBusCache;
import in.koreatech.koin.domain.bus.service.express.model.ExpressBusCacheInfo;
import in.koreatech.koin.domain.bus.service.express.model.ExpressBusRoute;
import in.koreatech.koin.domain.bus.service.express.model.ExpressBusTimetable;

@ExtendWith(MockitoExtension.class)
public class ExpressBusServiceTest {

    @InjectMocks
    private ExpressBusService expressBusService;

    @Mock
    private ExpressBusCacheRepository expressBusCacheRepository;

    private String busType;
    private BusStation departKoreatech;
    private BusStation departTerminal;
    private BusStation arrivalKoreatech;
    private BusStation arrivalTerminal;
    private LocalDateTime targetTime;
    private LocalTime arrivalTime1, arrivalTime2, arrivalTime3;
    private LocalTime departTime1, departTime2, departTime3;
    private ExpressBusCache cacheFromKoreatech;
    private ExpressBusCache cacheFromTerminal;
    private ExpressBusCache cacheWithThreeItems;

    @BeforeEach
    void setUp() {
        busType = "express";
        departKoreatech = BusStation.KOREATECH;
        arrivalTerminal = BusStation.TERMINAL;
        departTerminal = BusStation.TERMINAL;
        arrivalKoreatech = BusStation.KOREATECH;

        arrivalTime1 = LocalTime.of(9, 30);
        arrivalTime2 = LocalTime.of(10, 30);
        arrivalTime3 = LocalTime.of(12, 0);

        departTime1 = LocalTime.of(9, 0);
        departTime2 = LocalTime.of(10, 0);
        departTime3 = LocalTime.of(11, 30);

        ExpressBusRoute routeFromKoreatechToTerminal = new ExpressBusRoute(departKoreatech.getName(), arrivalTerminal.getName());
        ExpressBusRoute routeFromTerminalToKoreatech = new ExpressBusRoute(departTerminal.getName(), arrivalKoreatech.getName());

        // 2개 항목이 있는 버스 정보
        List<ExpressBusCacheInfo> busInfos = Arrays.asList(
            new ExpressBusCacheInfo(departTime1, arrivalTime1, 5000),
            new ExpressBusCacheInfo(departTime2, arrivalTime2, 5000)
        );

        // 3개 항목이 있는 버스 정보
        List<ExpressBusCacheInfo> busInfosWithThreeItems = Arrays.asList(
            new ExpressBusCacheInfo(departTime1, arrivalTime1, 5000),
            new ExpressBusCacheInfo(departTime2, arrivalTime2, 5000),
            new ExpressBusCacheInfo(departTime3, arrivalTime3, 5000)
        );

        // 각 방향별 캐시 생성
        cacheFromKoreatech = ExpressBusCache.of(routeFromKoreatechToTerminal, busInfos);
        cacheFromTerminal = ExpressBusCache.of(routeFromTerminalToKoreatech, busInfos);
        cacheWithThreeItems = ExpressBusCache.of(routeFromKoreatechToTerminal, busInfosWithThreeItems);
    }

    @Nested
    @DisplayName("버스 시간 검색 테스트")
    class searchBusTimeTest {

        @Test
        void 캐시가_존재하면_가장_가까운_출발_시간을_반환한다() {
            //given
            targetTime = LocalDateTime.of(2024, 6, 10, 10, 0);
            when(expressBusCacheRepository
                .findById(ExpressBusCache.generateId(
                    new ExpressBusRoute(departKoreatech.getName(), arrivalTerminal.getName()))))
                .thenReturn(Optional.of(cacheWithThreeItems));

            //when
            SingleBusTimeResponse response = expressBusService.searchBusTime(busType, departKoreatech, arrivalTerminal,
                targetTime);

            //then
            assertThat(response).isNotNull();
            assertThat(response.busName()).isEqualTo(BusType.EXPRESS.getName());
            assertThat(response.busTime()).isEqualTo(departTime3); // 11:30이 가장 가까운 출발 시간
        }

        @Test
        void 캐시가_존재하지_않으면_null을_반환한다() {
            //given
            when(expressBusCacheRepository
                .findById(ExpressBusCache.generateId(
                    new ExpressBusRoute(departKoreatech.getName(), arrivalTerminal.getName()))))
                .thenReturn(Optional.empty());

            //when
            SingleBusTimeResponse response = expressBusService.searchBusTime(busType, departKoreatech, arrivalTerminal,
                targetTime);

            //then
            assertThat(response).isNull();
        }

        @Test
        void 버스_출발_시간이_모두_타겟_시간_이전이면_버스시간이_NULL인_객체를_반환한다() {
            //given
            LocalDateTime targetTime = LocalDateTime.of(2024, 6, 10, 13, 0);
            when(expressBusCacheRepository
                .findById(ExpressBusCache.generateId(
                    new ExpressBusRoute(departKoreatech.getName(), arrivalTerminal.getName()))))
                .thenReturn(Optional.of(cacheWithThreeItems));

            //when
            SingleBusTimeResponse response = expressBusService.searchBusTime(busType, departKoreatech, arrivalTerminal,
                targetTime);

            //then
            assertThat(response).isNotNull();
            assertThat(response.busName()).isEqualTo(BusType.EXPRESS.getName());
            assertThat(response.busTime()).isNull();
        }
    }

    @Nested
    @DisplayName("고속버스 시간표 조회 테스트")
    class getExpressBusTimetableTest {

        @Test
        void from_방향의_시간표를_조회한다() {
            //given
            String direction = "from";
            when(expressBusCacheRepository
                .getById(ExpressBusCache.generateId(
                    new ExpressBusRoute(BusStation.KOREATECH.getName(), BusStation.TERMINAL.getName()))))
                .thenReturn(cacheFromKoreatech);

            //when
            List<ExpressBusTimetable> timetables = expressBusService.getExpressBusTimetable(
                direction);

            //then
            assertThat(timetables).hasSize(2);

            // 첫 번째 버스 시간표
            ExpressBusTimetable firstTimetable = timetables.get(0);
            assertThat(firstTimetable.getDepart()).isEqualTo(
                departTime1.format(DateTimeFormatter.ofPattern("HH:mm")));
            assertThat(firstTimetable.getArrival()).isEqualTo(
                arrivalTime1.format(DateTimeFormatter.ofPattern("HH:mm")));
            assertThat(firstTimetable.getCharge()).isEqualTo(5000);

            // 두 번째 버스 시간표
            ExpressBusTimetable secondTimetable = timetables.get(1);
            assertThat(secondTimetable.getDepart()).isEqualTo(
                departTime2.format(DateTimeFormatter.ofPattern("HH:mm")));
            assertThat(secondTimetable.getArrival()).isEqualTo(
                arrivalTime2.format(DateTimeFormatter.ofPattern("HH:mm")));
            assertThat(secondTimetable.getCharge()).isEqualTo(5000);
        }

        @Test
        void to_방향의_시간표를_조회한다() {
            //given
            String direction = "to";
            when(expressBusCacheRepository
                .getById(ExpressBusCache.generateId(
                    new ExpressBusRoute(BusStation.TERMINAL.getName(), BusStation.KOREATECH.getName()))))
                .thenReturn(cacheFromTerminal);

            //when
            List<ExpressBusTimetable> timetables = expressBusService.getExpressBusTimetable(
                direction);

            //then
            assertThat(timetables).hasSize(2);

            // 첫 번째 버스 시간표
            ExpressBusTimetable firstTimetable = timetables.get(0);
            assertThat(firstTimetable.getDepart()).isEqualTo(
                departTime1.format(DateTimeFormatter.ofPattern("HH:mm")));
            assertThat(firstTimetable.getArrival()).isEqualTo(
                arrivalTime1.format(DateTimeFormatter.ofPattern("HH:mm")));
            assertThat(firstTimetable.getCharge()).isEqualTo(5000);

            // 두 번째 버스 시간표
            ExpressBusTimetable secondTimetable = timetables.get(1);
            assertThat(secondTimetable.getDepart()).isEqualTo(
                departTime2.format(DateTimeFormatter.ofPattern("HH:mm")));
            assertThat(secondTimetable.getArrival()).isEqualTo(
                arrivalTime2.format(DateTimeFormatter.ofPattern("HH:mm")));
            assertThat(secondTimetable.getCharge()).isEqualTo(5000);
        }

        @Test
        void 지원하지_않는_방향이면_예외가_발생한다() {
            //given
            String direction = "invalid";

            //when & then
            assertThatThrownBy(() -> expressBusService.getExpressBusTimetable(direction))
                .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void 캐시가_존재하지_않으면_빈_목록을_반환한다() {
            //given
            String direction = "from";
            when(expressBusCacheRepository
                .getById(ExpressBusCache.generateId(
                    new ExpressBusRoute(BusStation.KOREATECH.getName(), BusStation.TERMINAL.getName()))))
                .thenReturn(null);

            //when
            List<ExpressBusTimetable> timetables = expressBusService.getExpressBusTimetable(
                direction);

            //then
            assertThat(timetables).isEmpty();
        }
    }

    @Nested
    @DisplayName("버스 잔여 시간 조회 테스트")
    class GetBusRemainTimeTest {

        private Clock fixedClock;
        private ZoneId koreaZoneId;

        @BeforeEach
        void setUpClock() {
            // 한국 시간대 고정시간
            koreaZoneId = ZoneId.of("Asia/Seoul");
            fixedClock = Clock.fixed(
                LocalDateTime.of(2024, 6, 10, 10, 0).atZone(koreaZoneId).toInstant(),
                koreaZoneId
            );
        }

        @Test
        void 출발지와_도착지에_맞는_버스_잔여_시간을_반환한다() {
            // given
            when(expressBusCacheRepository.findById(ExpressBusCache.generateId(
                new ExpressBusRoute(departKoreatech.getName(), arrivalTerminal.getName()))))
                .thenReturn(Optional.of(cacheWithThreeItems));

            // when
            List<ExpressBusRemainTime> result = expressBusService.getBusRemainTime(departKoreatech, arrivalTerminal);

            // then
            assertThat(fixedClock.instant().atZone(koreaZoneId).toLocalTime()).isEqualTo(LocalTime.of(10, 0));
            assertThat(result.get(0).getRemainSeconds(fixedClock)).isNull(); // 9시는 10시보다 이전이므로 null
            assertThat(result.get(1).getRemainSeconds(fixedClock)).isNull(); // 10시는 현재 시간과 동일
            assertThat(result.get(2).getRemainSeconds(fixedClock)).isEqualTo(5400L); // 11시 30분은 1시간 30분(5400초) 차이
        }

        @Test
        void 캐시가_존재하지_않으면_빈_목록을_반환한다() {
            // given
            when(expressBusCacheRepository.findById(ExpressBusCache.generateId(
                new ExpressBusRoute(departKoreatech.getName(), arrivalTerminal.getName()))))
                .thenReturn(Optional.empty());

            // when
            List<ExpressBusRemainTime> result = expressBusService.getBusRemainTime(departKoreatech, arrivalTerminal);

            // then
            assertThat(result).isEmpty();
        }
    }
}
