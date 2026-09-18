package in.koreatech.koin.unit.domain.coopshop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.domain.coopshop.model.CoopName;
import in.koreatech.koin.domain.coopshop.model.CoopSemester;
import in.koreatech.koin.domain.coopshop.model.CoopShop;
import in.koreatech.koin.domain.coopshop.repository.CoopNameRepository;
import in.koreatech.koin.domain.coopshop.repository.CoopOpenRepository;
import in.koreatech.koin.domain.coopshop.repository.CoopSemesterRepository;
import in.koreatech.koin.domain.coopshop.repository.CoopShopRepository;
import in.koreatech.koin.domain.coopshop.service.CoopShopService;
import in.koreatech.koin.global.exception.custom.DataNotFoundException;

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

    private final Clock clock = Clock.fixed(
        Instant.parse("2026-09-18T00:05:00Z"),
        ZoneId.of("Asia/Seoul")
    );

    @BeforeEach
    void setUp() {
        coopShopService = new CoopShopService(
            clock, coopShopRepository, coopOpenRepository, coopSemesterRepository, coopNameRepository
        );
    }

    private CoopSemester 학기(String semester, LocalDate fromDate, LocalDate toDate, boolean 매장있음) {
        CoopSemester coopSemester = CoopSemester.of(semester, fromDate, toDate);
        if (매장있음) {
            CoopShop coopShop = CoopShop.builder()
                .coopName(CoopName.builder().name("학생식당").build())
                .location("학생회관 1층")
                .phone("041-000-0000")
                .build();
            coopSemester.replaceCoopShops(List.of(coopShop));
        }
        return coopSemester;
    }

    @Test
    @DisplayName("현재 적용된 학기가 아직 유효하면 전환하지 않는다")
    void 현재_학기가_유효하면_전환하지_않는다() {
        CoopSemester currentSemester = 학기("26-2학기", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 18), true);
        currentSemester.updateApply(true);
        when(coopSemesterRepository.getByIsApplied(true)).thenReturn(currentSemester);

        coopShopService.updateSemester();

        assertThat(currentSemester.isApplied()).isTrue();
    }

    @Test
    @DisplayName("다음 학기에 매장 데이터가 없으면 전환을 보류하고 예외를 던진다")
    void 다음_학기에_매장이_없으면_전환하지_않는다() {
        CoopSemester expiredSemester = 학기("26-1학기", LocalDate.of(2026, 3, 3), LocalDate.of(2026, 6, 19), true);
        CoopSemester emptyNextSemester = 학기("26-2학기", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 18), false);
        when(coopSemesterRepository.getByIsApplied(true)).thenReturn(expiredSemester);
        when(coopSemesterRepository.getTopByOrderByToDateDesc()).thenReturn(emptyNextSemester);

        assertThatThrownBy(() -> coopShopService.updateSemester())
            .isInstanceOf(DataNotFoundException.class)
            .hasMessageContaining("해당 학기가 존재하지 않습니다");
    }

    @Test
    @DisplayName("다음 학기에 매장 데이터가 있으면 정상적으로 전환된다")
    void 다음_학기에_매장이_있으면_전환된다() {
        CoopSemester expiredSemester = 학기("26-1학기", LocalDate.of(2026, 3, 3), LocalDate.of(2026, 6, 19), true);
        CoopSemester nextSemester = 학기("26-2학기", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 18), true);
        when(coopSemesterRepository.getByIsApplied(true)).thenReturn(expiredSemester);
        when(coopSemesterRepository.getTopByOrderByToDateDesc()).thenReturn(nextSemester);

        coopShopService.updateSemester();

        assertThat(nextSemester.isApplied()).isTrue();
    }
}
