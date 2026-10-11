package in.koreatech.koin.unit.domain.coopshop.service;

import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_COOP_SEMESTER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.domain.coopshop.model.CoopSemester;
import in.koreatech.koin.domain.coopshop.model.CoopShop;
import in.koreatech.koin.domain.coopshop.repository.CoopNameRepository;
import in.koreatech.koin.domain.coopshop.repository.CoopOpenRepository;
import in.koreatech.koin.domain.coopshop.repository.CoopSemesterRepository;
import in.koreatech.koin.domain.coopshop.repository.CoopShopRepository;
import in.koreatech.koin.domain.coopshop.service.CoopShopService;
import in.koreatech.koin.global.exception.CustomException;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class CoopShopServiceTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final LocalDate TODAY = LocalDate.of(2024, 1, 15);

    @Mock
    private CoopShopRepository coopShopRepository;

    @Mock
    private CoopOpenRepository coopOpenRepository;

    @Mock
    private CoopSemesterRepository coopSemesterRepository;

    @Mock
    private CoopNameRepository coopNameRepository;

    private CoopShopService coopShopService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TODAY.atStartOfDay(KST).toInstant(), KST);
        coopShopService = new CoopShopService(
            clock,
            coopShopRepository,
            coopOpenRepository,
            coopSemesterRepository,
            coopNameRepository
        );
    }

    @Test
    void 현재_학기가_유효하면_학기를_전환하지_않는다() {
        CoopSemester currentSemester = 매장이_있는_학기(
            "23-겨울학기", LocalDate.of(2023, 12, 21), LocalDate.of(2024, 2, 28)
        );
        currentSemester.updateApply(true);
        when(coopSemesterRepository.getByIsApplied(true)).thenReturn(currentSemester);

        coopShopService.updateSemester();

        assertThat(currentSemester.isApplied()).isTrue();
        verify(coopSemesterRepository, never()).findAllValidOn(any());
    }

    @Test
    void 현재_학기가_날짜는_유효해도_매장이_없으면_다른_학기를_찾는다() {
        CoopSemester currentEmptySemester = CoopSemester.of(
            "23-겨울학기", LocalDate.of(2023, 12, 21), LocalDate.of(2024, 2, 28)
        );
        currentEmptySemester.updateApply(true);
        CoopSemester overlappingServingSemester = 매장이_있는_학기(
            "24-1학기", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30)
        );
        when(coopSemesterRepository.getByIsApplied(true)).thenReturn(currentEmptySemester);
        when(coopSemesterRepository.findAllValidOn(TODAY))
            .thenReturn(List.of(overlappingServingSemester, currentEmptySemester));

        coopShopService.updateSemester();

        assertThat(currentEmptySemester.isApplied()).isFalse();
        assertThat(overlappingServingSemester.isApplied()).isTrue();
    }

    @Test
    void 현재_학기가_날짜는_유효해도_매장이_없고_대체할_학기도_없으면_예외가_발생한다() {
        CoopSemester currentEmptySemester = CoopSemester.of(
            "23-겨울학기", LocalDate.of(2023, 12, 21), LocalDate.of(2024, 2, 28)
        );
        currentEmptySemester.updateApply(true);
        when(coopSemesterRepository.getByIsApplied(true)).thenReturn(currentEmptySemester);
        when(coopSemesterRepository.findAllValidOn(TODAY)).thenReturn(List.of(currentEmptySemester));

        assertThatThrownBy(() -> coopShopService.updateSemester())
            .isInstanceOf(CustomException.class)
            .hasMessage(NOT_FOUND_COOP_SEMESTER.getMessage());
        assertThat(currentEmptySemester.isApplied()).isTrue();
    }

    @Test
    void 만료된_경우_오늘이_포함되고_매장이_있는_학기로_전환한다() {
        CoopSemester expiredSemester = 매장이_있는_학기(
            "23-2학기", LocalDate.of(2023, 9, 2), LocalDate.of(2023, 12, 20)
        );
        expiredSemester.updateApply(true);
        CoopSemester emptySemester = CoopSemester.of(
            "24-1학기", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30)
        );
        CoopSemester servingSemester = 매장이_있는_학기(
            "23-겨울학기", LocalDate.of(2023, 12, 21), LocalDate.of(2024, 2, 28)
        );
        when(coopSemesterRepository.getByIsApplied(true)).thenReturn(expiredSemester);
        when(coopSemesterRepository.findAllValidOn(TODAY)).thenReturn(List.of(emptySemester, servingSemester));

        coopShopService.updateSemester();

        assertThat(expiredSemester.isApplied()).isFalse();
        assertThat(emptySemester.isApplied()).isFalse();
        assertThat(servingSemester.isApplied()).isTrue();
    }

    @Test
    void 오늘이_포함되고_매장이_있는_학기가_없으면_예외가_발생한다() {
        CoopSemester expiredSemester = 매장이_있는_학기(
            "23-2학기", LocalDate.of(2023, 9, 2), LocalDate.of(2023, 12, 20)
        );
        expiredSemester.updateApply(true);
        CoopSemester emptySemester = CoopSemester.of(
            "24-1학기", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30)
        );
        when(coopSemesterRepository.getByIsApplied(true)).thenReturn(expiredSemester);
        when(coopSemesterRepository.findAllValidOn(TODAY)).thenReturn(List.of(emptySemester));

        assertThatThrownBy(() -> coopShopService.updateSemester())
            .isInstanceOf(CustomException.class)
            .hasMessage(NOT_FOUND_COOP_SEMESTER.getMessage());
        assertThat(expiredSemester.isApplied()).isTrue();
    }

    @Test
    void 학기_시작일과_종료일_당일도_유효한_학기로_판단한다() {
        CoopSemester startsToday = CoopSemester.of("24-1학기", TODAY, LocalDate.of(2024, 6, 30));
        CoopSemester endsToday = CoopSemester.of("23-겨울학기", LocalDate.of(2023, 12, 21), TODAY);
        CoopSemester endedYesterday = CoopSemester.of(
            "23-2학기", LocalDate.of(2023, 9, 2), TODAY.minusDays(1)
        );

        assertThat(coopShopService.validateSemester(startsToday)).isTrue();
        assertThat(coopShopService.validateSemester(endsToday)).isTrue();
        assertThat(coopShopService.validateSemester(endedYesterday)).isFalse();
    }

    private CoopSemester 매장이_있는_학기(String semester, LocalDate fromDate, LocalDate toDate) {
        CoopSemester coopSemester = CoopSemester.of(semester, fromDate, toDate);
        coopSemester.replaceCoopShops(List.of(
            CoopShop.builder()
                .location("학생회관 1층")
                .phone("041-000-0000")
                .build()
        ));
        return coopSemester;
    }
}
