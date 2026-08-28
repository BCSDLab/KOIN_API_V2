package in.koreatech.koin.domain.team.recruitment.scheduler;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "team-recruitment.outbox")
public class TeamRecruitmentOutboxProperties {

    private boolean enabled;
    private long fixedDelayMs = 10_000L;
    private int batchSize = 20;
    private int maxAttempts = 5;
    private int leaseSeconds = 120;
    private int retryBackoffSeconds = 30;
    private int maxRetryBackoffSeconds = 3_600;

    public int getBoundedBatchSize() {
        return Math.min(Math.max(batchSize, 1), 100);
    }

    public int getBoundedMaxAttempts() {
        return Math.min(Math.max(maxAttempts, 1), 100);
    }

    public int getBoundedLeaseSeconds() {
        return Math.min(Math.max(leaseSeconds, 1), 86_400);
    }

    public int getBoundedRetryBackoffSeconds() {
        return Math.min(Math.max(retryBackoffSeconds, 1), 86_400);
    }

    public int getBoundedMaxRetryBackoffSeconds() {
        return Math.max(
            getBoundedRetryBackoffSeconds(),
            Math.min(Math.max(maxRetryBackoffSeconds, 1), 86_400)
        );
    }
}
