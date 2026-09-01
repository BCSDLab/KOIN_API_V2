package in.koreatech.koin.domain.team.recruitment.scheduler;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentOutboxEvent;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentOutboxEventRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamRecruitmentOutboxLeaseService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final TeamRecruitmentOutboxEventRepository outboxEventRepository;
    private final TeamRecruitmentOutboxProperties properties;
    private final Clock clock;

    @Transactional
    public List<OutboxClaim> claim(String workerId, int requestedLimit) {
        if (requestedLimit <= 0) {
            return List.of();
        }
        int limit = Math.min(requestedLimit, properties.getBoundedBatchSize());
        int maxAttempts = properties.getBoundedMaxAttempts();
        LocalDateTime now = now();
        List<TeamRecruitmentOutboxEvent> events = outboxEventRepository.findClaimableForUpdate(
            now,
            limit,
            maxAttempts
        );
        if (events == null || events.isEmpty()) {
            return List.of();
        }

        LocalDateTime lockedUntil = now.plusSeconds(properties.getBoundedLeaseSeconds());
        List<OutboxClaim> claims = new ArrayList<>(events.size());
        for (TeamRecruitmentOutboxEvent event : events) {
            if (event.getAttemptCount() >= maxAttempts) {
                event.markTerminalFailure("MAX_ATTEMPTS_EXCEEDED", maxAttempts);
                continue;
            }
            event.markProcessing(workerId, lockedUntil);
            claims.add(new OutboxClaim(event.getId(), event.getEventKey(), event.getPayload()));
        }
        return claims;
    }

    @Transactional
    public boolean complete(Integer eventId, String workerId) {
        return outboxEventRepository.findByIdWithLock(eventId)
            .filter(event -> event.isProcessingBy(workerId))
            .map(event -> {
                event.markPublished(now());
                return true;
            })
            .orElse(false);
    }

    @Transactional
    public boolean fail(
        Integer eventId,
        String workerId,
        String reason,
        boolean retryable
    ) {
        return outboxEventRepository.findByIdWithLock(eventId)
            .filter(event -> event.isProcessingBy(workerId))
            .map(event -> {
                LocalDateTime now = now();
                String safeReason = sanitize(reason);
                if (!retryable || event.getAttemptCount() >= properties.getBoundedMaxAttempts()) {
                    event.markTerminalFailure(safeReason, properties.getBoundedMaxAttempts());
                    return true;
                }

                event.markFailed(safeReason, now.plusSeconds(resolveBackoffSeconds(event.getAttemptCount())));
                return true;
            })
            .orElse(false);
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock.withZone(KST));
    }

    private long resolveBackoffSeconds(int attemptCount) {
        long base = properties.getBoundedRetryBackoffSeconds();
        long max = properties.getBoundedMaxRetryBackoffSeconds();
        int exponent = Math.max(0, Math.min(attemptCount - 1, 30));
        long multiplier = 1L << exponent;
        if (base > max / multiplier) {
            return max;
        }
        return Math.min(max, base * multiplier);
    }

    private String sanitize(String reason) {
        if (reason == null || reason.isBlank()) {
            return "UNKNOWN_FAILURE";
        }
        return reason.length() <= 500 ? reason : reason.substring(0, 500);
    }

    public record OutboxClaim(Integer id, String eventKey, String payload) {
    }
}
