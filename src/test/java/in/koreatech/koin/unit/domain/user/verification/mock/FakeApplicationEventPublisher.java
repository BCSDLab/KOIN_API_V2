package in.koreatech.koin.unit.domain.user.verification.mock;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;

public class FakeApplicationEventPublisher implements ApplicationEventPublisher {

    public final List<Object> publishedEvents = new ArrayList<>();

    @Override
    public void publishEvent(Object event) {
        this.publishedEvents.add(event);
    }
}
