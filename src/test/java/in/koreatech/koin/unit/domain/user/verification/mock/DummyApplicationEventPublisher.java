package in.koreatech.koin.unit.domain.user.verification.mock;

import org.springframework.context.ApplicationEventPublisher;

public class DummyApplicationEventPublisher implements ApplicationEventPublisher {

    @Override
    public void publishEvent(Object event) {
        // do nothing
    }
}
