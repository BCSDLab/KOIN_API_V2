package in.koreatech.koin.unit.domain.team.recruitment.scheduler;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentOutboxEventStatus.FAILED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentOutboxEventStatus.PUBLISHED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentOutboxEventStatus.PROCESSING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentOutboxEvent;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentOutboxEventRepository;
import in.koreatech.koin.domain.team.recruitment.scheduler.TeamRecruitmentOutboxLeaseService;
import in.koreatech.koin.domain.team.recruitment.scheduler.TeamRecruitmentOutboxProperties;

@ExtendWith(MockitoExtension.class)
class TeamRecruitmentOutboxLeaseServiceTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 28, 12, 0);

    @Mock
    private TeamRecruitmentOutboxEventRepository outboxEventRepository;

    @Spy
    private TeamRecruitmentOutboxProperties properties = new TeamRecruitmentOutboxProperties();

    @Mock
    private Clock clock;

    @InjectMocks
    private TeamRecruitmentOutboxLeaseService leaseService;

    @BeforeEach
    void setUp() {
        properties.setBatchSize(10);
        properties.setMaxAttempts(3);
        properties.setLeaseSeconds(120);
        properties.setRetryBackoffSeconds(30);
        properties.setMaxRetryBackoffSeconds(300);
        lenient().when(clock.withZone(KST)).thenReturn(Clock.fixed(Instant.parse("2026-08-28T03:00:00Z"), KST));
    }

    @Test
    void claim은_만료_행을_worker와_lease로_점유하고_attempt를_증가시킨다() {
        TeamRecruitmentOutboxEvent event = event();
        when(outboxEventRepository.findClaimableForUpdate(any(), any(Integer.class), any(Integer.class)))
            .thenReturn(List.of(event));

        List<TeamRecruitmentOutboxLeaseService.OutboxClaim> claims = leaseService.claim("worker-1", 10);

        assertThat(claims).hasSize(1);
        assertThat(event.getStatus()).isEqualTo(PROCESSING);
        assertThat(event.getWorkerId()).isEqualTo("worker-1");
        assertThat(event.getAttemptCount()).isEqualTo(1);
        assertThat(event.getLockedUntil()).isEqualTo(NOW.plusSeconds(120));
    }

    @Test
    void complete와_fail은_현재_worker만_상태를_변경한다() {
        TeamRecruitmentOutboxEvent event = event();
        event.markProcessing("worker-1", NOW.plusSeconds(120));
        when(outboxEventRepository.findByIdWithLock(1)).thenReturn(Optional.of(event));

        assertThat(leaseService.complete(1, "worker-2")).isFalse();
        assertThat(event.getStatus()).isEqualTo(PROCESSING);
        assertThat(leaseService.complete(1, "worker-1")).isTrue();
        assertThat(event.getStatus()).isEqualTo(PUBLISHED);
        assertThat(event.getLockedUntil()).isNull();

        event.markProcessing("worker-1", NOW.plusSeconds(120));
        assertThat(leaseService.fail(1, "worker-2", "INTERNAL", true)).isFalse();
        assertThat(event.getStatus()).isEqualTo(PROCESSING);
    }

    @Test
    void transient_failure은_backoff으로_재시도하고_permanent_failure은_terminal_failed가_된다() {
        TeamRecruitmentOutboxEvent event = event();
        event.markProcessing("worker-1", NOW.plusSeconds(120));
        when(outboxEventRepository.findByIdWithLock(1)).thenReturn(Optional.of(event));

        assertThat(leaseService.fail(1, "worker-1", "INTERNAL", true)).isTrue();
        assertThat(event.getStatus()).isEqualTo(FAILED);
        assertThat(event.getNextAttemptAt()).isEqualTo(NOW.plusSeconds(30));
        assertThat(event.getWorkerId()).isNull();

        event.markProcessing("worker-1", NOW.plusSeconds(120));
        assertThat(leaseService.fail(1, "worker-1", "NO_DEVICE_TOKEN", false)).isTrue();
        assertThat(event.getStatus()).isEqualTo(FAILED);
        assertThat(event.getAttemptCount()).isEqualTo(3);
        assertThat(event.getNextAttemptAt()).isNull();
    }

    @Test
    void publishedAt은_주입된_시간을_요구한다() {
        TeamRecruitmentOutboxEvent event = event();
        event.markProcessing("worker-1", NOW.plusSeconds(120));

        event.markPublished(NOW);

        assertThat(event.getPublishedAt()).isEqualTo(NOW);
    }

    private TeamRecruitmentOutboxEvent event() {
        return TeamRecruitmentOutboxEvent.builder()
            .id(1)
            .eventKey("event-key")
            .eventType("TEAM_RECRUITMENT_NOTIFICATION")
            .aggregateType("TEAM_RECRUITMENT")
            .aggregateId(10)
            .payload("{}")
            .build();
    }
}
