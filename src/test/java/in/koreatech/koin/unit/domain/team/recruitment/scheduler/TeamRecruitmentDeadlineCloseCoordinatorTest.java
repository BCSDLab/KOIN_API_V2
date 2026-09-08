package in.koreatech.koin.unit.domain.team.recruitment.scheduler;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.RECRUITING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;
import in.koreatech.koin.domain.team.recruitment.scheduler.TeamRecruitmentDeadlineCloseCoordinator;
import in.koreatech.koin.domain.team.recruitment.scheduler.TeamRecruitmentDeadlineCloseProcessor;
import in.koreatech.koin.unit.fixture.UserFixture;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class TeamRecruitmentDeadlineCloseCoordinatorTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final LocalDate TODAY = LocalDate.of(2026, 8, 28);

    @Mock
    private TeamRecruitmentRepository recruitmentRepository;

    @Mock
    private TeamRecruitmentDeadlineCloseProcessor closeProcessor;

    @Mock
    private Clock clock;

    @InjectMocks
    private TeamRecruitmentDeadlineCloseCoordinator coordinator;

    @BeforeEach
    void setUpClock() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-08-28T03:00:00Z"), KST);
        when(clock.withZone(KST)).thenReturn(fixedClock);
    }

    @Test
    void 한_모집의_마감이_실패해도_다음_모집을_계속_처리한다() {
        TeamRecruitment first = recruitment(1);
        TeamRecruitment second = recruitment(2);
        when(recruitmentRepository.findAllByStatusAndDeadlineDateBefore(
            eq(RECRUITING),
            eq(TODAY),
            any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(first, second)));
        doThrow(new IllegalStateException("마감 실패"))
            .when(closeProcessor).closeIfExpired(1, TODAY);

        coordinator.closeExpiredRecruitments();

        InOrder processingOrder = inOrder(closeProcessor);
        processingOrder.verify(closeProcessor).closeIfExpired(1, TODAY);
        processingOrder.verify(closeProcessor).closeIfExpired(2, TODAY);
    }

    @Test
    void 한_번에_ID_오름차순으로_최대_100개를_조회한다() {
        when(recruitmentRepository.findAllByStatusAndDeadlineDateBefore(
            eq(RECRUITING),
            eq(TODAY),
            any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of()));

        coordinator.closeExpiredRecruitments();

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(recruitmentRepository).findAllByStatusAndDeadlineDateBefore(
            eq(RECRUITING),
            eq(TODAY),
            pageableCaptor.capture()
        );
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(100);
        assertThat(pageable.getSort().getOrderFor("id"))
            .extracting(Sort.Order::getDirection)
            .isEqualTo(Sort.Direction.ASC);
    }

    private TeamRecruitment recruitment(Integer id) {
        return TeamRecruitment.builder()
            .id(id)
            .author(UserFixture.id_설정_코인_유저(id))
            .activityStartDate(TODAY.plusDays(1))
            .activityEndDate(TODAY.plusDays(10))
            .deadlineDate(TODAY.minusDays(1))
            .maxParticipants(5)
            .currentParticipants(0)
            .status(RECRUITING)
            .description("모집 내용")
            .build();
    }
}
