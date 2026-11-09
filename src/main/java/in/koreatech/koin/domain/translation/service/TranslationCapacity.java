package in.koreatech.koin.domain.translation.service;

import java.time.Clock;
import java.util.concurrent.Semaphore;
import org.springframework.stereotype.Component;

/** 새 AI 호출만 제한한다. 저장된 결과와 진행 상태 조회에는 적용하지 않는다. */
@Component
public class TranslationCapacity {
    private final Semaphore permits;
    private final int requestsPerMinute;
    private final Clock clock;
    private long windowStarted;
    private int requests;

    public TranslationCapacity(TranslationProperties properties, Clock clock) {
        this.permits = new Semaphore(properties.getMaxConcurrentRequests());
        this.requestsPerMinute = properties.getMaxRequestsPerMinute();
        this.clock = clock;
        this.windowStarted = clock.millis();
    }

    public synchronized boolean tryAcquire() {
        long now = clock.millis();
        if (now - windowStarted >= 60_000 || now < windowStarted) {
            windowStarted = now;
            requests = 0;
        }
        if (requests >= requestsPerMinute || !permits.tryAcquire()) {
            return false;
        }
        requests++;
        return true;
    }

    public void release() {
        permits.release();
    }
}
