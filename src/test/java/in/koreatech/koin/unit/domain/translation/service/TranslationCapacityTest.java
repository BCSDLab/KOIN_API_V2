package in.koreatech.koin.unit.domain.translation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.Clock;
import org.junit.jupiter.api.Test;
import in.koreatech.koin.domain.translation.service.TranslationCapacity;
import in.koreatech.koin.domain.translation.service.TranslationProperties;

class TranslationCapacityTest {
    @Test
    void simultaneousRequestsAreBoundedAndRejectedRequestsDoNotSpendQuota() {
        Clock clock = mock(Clock.class);
        var properties = new TranslationProperties();
        properties.setMaxConcurrentRequests(1);
        properties.setMaxRequestsPerMinute(2);
        var capacity = new TranslationCapacity(properties, clock);
        assertThat(capacity.tryAcquire()).isTrue();
        assertThat(capacity.tryAcquire()).isFalse();
        capacity.release();
        assertThat(capacity.tryAcquire()).isTrue();
        capacity.release();
        assertThat(capacity.tryAcquire()).isFalse();
        when(clock.millis()).thenReturn(60_000L);
        assertThat(capacity.tryAcquire()).isTrue();
    }
}
