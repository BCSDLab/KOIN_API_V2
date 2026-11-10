package in.koreatech.koin.config;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

/**
 * 테스트 컨텍스트 전체가 공유하는 고정 시계.
 * 시각을 바꾼 테스트 클래스가 끝나면 {@link #reset()}으로 기본 시각으로 되돌린다.
 */
public class TestClock extends Clock {

    private final Clock defaultClock;
    private volatile Clock current;

    public TestClock(Instant instant, ZoneId zone) {
        this.defaultClock = Clock.fixed(instant, zone);
        this.current = defaultClock;
    }

    public void fix(Instant instant, ZoneId zone) {
        current = Clock.fixed(instant, zone);
    }

    public void advance(Duration duration) {
        Clock snapshot = current;
        current = Clock.fixed(snapshot.instant().plus(duration), snapshot.getZone());
    }

    public void reset() {
        current = defaultClock;
    }

    @Override
    public ZoneId getZone() {
        return current.getZone();
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return current.withZone(zone);
    }

    @Override
    public Instant instant() {
        return current.instant();
    }
}
