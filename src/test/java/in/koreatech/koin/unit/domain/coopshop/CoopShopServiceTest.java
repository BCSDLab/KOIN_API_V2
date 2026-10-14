package in.koreatech.koin.unit.domain.coopshop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.domain.coopshop.exception.CoopOpenNotFoundException;
import in.koreatech.koin.domain.coopshop.model.CoopName;
import in.koreatech.koin.domain.coopshop.model.CoopOpen;
import in.koreatech.koin.domain.coopshop.model.CoopSemester;
import in.koreatech.koin.domain.coopshop.model.CoopShop;
import in.koreatech.koin.domain.coopshop.model.CoopShopType;
import in.koreatech.koin.domain.coopshop.model.DayType;
import in.koreatech.koin.domain.coopshop.repository.CoopNameRepository;
import in.koreatech.koin.domain.coopshop.repository.CoopOpenRepository;
import in.koreatech.koin.domain.coopshop.repository.CoopSemesterRepository;
import in.koreatech.koin.domain.coopshop.repository.CoopShopRepository;
import in.koreatech.koin.domain.coopshop.service.CoopShopService;
import in.koreatech.koin.domain.dining.model.DiningType;

@ExtendWith(MockitoExtension.class)
class CoopShopServiceTest {

    private CoopShopService coopShopService;

    @Mock
    private CoopShopRepository coopShopRepository;

    @Mock
    private CoopOpenRepository coopOpenRepository;

    @Mock
    private CoopSemesterRepository coopSemesterRepository;

    @Mock
    private CoopNameRepository coopNameRepository;

    private final Clock clock = Clock.systemDefaultZone();

    private CoopSemester currentSemester;
    private CoopShop cafeteria;
    private CoopName cafeteriaName;

    @BeforeEach
    void setUp() {
        coopShopService = new CoopShopService(
            clock, coopShopRepository, coopOpenRepository, coopSemesterRepository, coopNameRepository
        );

        currentSemester = CoopSemester.of("26-2학기", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 18));
        cafeteriaName = CoopName.builder().name("학생식당").build();
        cafeteria = CoopShop.builder().coopName(cafeteriaName).location("학생회관 1층").phone("041-000-0000").build();

        when(coopSemesterRepository.getByIsApplied(true)).thenReturn(currentSemester);
        when(coopNameRepository.getByName(CoopShopType.CAFETERIA)).thenReturn(cafeteriaName);
        when(coopShopRepository.getByCoopNameIdAndCoopSemester(cafeteriaName.getId(), currentSemester)).thenReturn(cafeteria);
    }

    private CoopOpen open(DayType dayType, String openTime, String closeTime) {
        return CoopOpen.builder()
            .coopShop(cafeteria)
            .type(DiningType.LUNCH.getDiningName())
            .dayOfWeek(dayType)
            .openTime(openTime)
            .closeTime(closeTime)
            .build();
    }

    @Test
    @DisplayName("평일은 기존대로 WEEKDAYS로 조회된다")
    void 평일은_WEEKDAYS로_조회된다() {
        LocalDateTime monday = LocalDateTime.of(2026, 9, 21, 12, 0);
        when(coopOpenRepository.findByCoopShopAndTypeAndDayOfWeek(cafeteria, DiningType.LUNCH.getDiningName(), DayType.WEEKDAYS))
            .thenReturn(Optional.of(open(DayType.WEEKDAYS, "11:30", "13:30")));

        boolean isOpened = coopShopService.getIsOpened(monday, CoopShopType.CAFETERIA, DiningType.LUNCH, false);

        assertThat(isOpened).isTrue();
    }

    @Test
    @DisplayName("토요일에 SATURDAY로 저장된 데이터가 있으면 그대로 조회된다")
    void 토요일_SATURDAY_데이터가_있으면_조회된다() {
        LocalDateTime saturday = LocalDateTime.of(2026, 9, 19, 12, 0);
        when(coopOpenRepository.findByCoopShopAndTypeAndDayOfWeek(cafeteria, DiningType.LUNCH.getDiningName(), DayType.SATURDAY))
            .thenReturn(Optional.of(open(DayType.SATURDAY, "11:30", "13:30")));

        boolean isOpened = coopShopService.getIsOpened(saturday, CoopShopType.CAFETERIA, DiningType.LUNCH, false);

        assertThat(isOpened).isTrue();
    }

    @Test
    @DisplayName("토요일에 SATURDAY 데이터가 없고 WEEKEND로만 저장돼 있으면 대체 조회된다")
    void 토요일_SATURDAY_없으면_WEEKEND로_대체_조회된다() {
        LocalDateTime saturday = LocalDateTime.of(2026, 9, 19, 12, 0);
        when(coopOpenRepository.findByCoopShopAndTypeAndDayOfWeek(cafeteria, DiningType.LUNCH.getDiningName(), DayType.SATURDAY))
            .thenReturn(Optional.empty());
        when(coopOpenRepository.findByCoopShopAndTypeAndDayOfWeek(cafeteria, DiningType.LUNCH.getDiningName(), DayType.WEEKEND))
            .thenReturn(Optional.of(open(DayType.WEEKEND, "11:30", "13:30")));

        boolean isOpened = coopShopService.getIsOpened(saturday, CoopShopType.CAFETERIA, DiningType.LUNCH, false);

        assertThat(isOpened).isTrue();
    }

    @Test
    @DisplayName("일요일에 SUNDAY 데이터가 없고 WEEKEND로만 저장돼 있으면 대체 조회된다")
    void 일요일_SUNDAY_없으면_WEEKEND로_대체_조회된다() {
        LocalDateTime sunday = LocalDateTime.of(2026, 9, 20, 12, 0);
        when(coopOpenRepository.findByCoopShopAndTypeAndDayOfWeek(cafeteria, DiningType.LUNCH.getDiningName(), DayType.SUNDAY))
            .thenReturn(Optional.empty());
        when(coopOpenRepository.findByCoopShopAndTypeAndDayOfWeek(cafeteria, DiningType.LUNCH.getDiningName(), DayType.WEEKEND))
            .thenReturn(Optional.of(open(DayType.WEEKEND, "11:30", "13:30")));

        boolean isOpened = coopShopService.getIsOpened(sunday, CoopShopType.CAFETERIA, DiningType.LUNCH, false);

        assertThat(isOpened).isTrue();
    }

    @Test
    @DisplayName("토요일에 SATURDAY도 WEEKEND도 없으면 예외가 발생한다")
    void 토요일_데이터가_전혀_없으면_예외() {
        LocalDateTime saturday = LocalDateTime.of(2026, 9, 19, 12, 0);
        when(coopOpenRepository.findByCoopShopAndTypeAndDayOfWeek(cafeteria, DiningType.LUNCH.getDiningName(), DayType.SATURDAY))
            .thenReturn(Optional.empty());
        when(coopOpenRepository.findByCoopShopAndTypeAndDayOfWeek(cafeteria, DiningType.LUNCH.getDiningName(), DayType.WEEKEND))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> coopShopService.getIsOpened(saturday, CoopShopType.CAFETERIA, DiningType.LUNCH, false))
            .isInstanceOf(CoopOpenNotFoundException.class);
    }
}
