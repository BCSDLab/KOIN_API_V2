package in.koreatech.koin.socket.domain.message.util;

import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebSocketSessionTracker {

    private final SimpUserRegistry simpUserRegistry;

    public int getSubscriptionCount(String destination) {
        return (int) simpUserRegistry
            .getUsers()
            .stream()
            .flatMap(user -> user.getSessions().stream())
            .flatMap(session -> session.getSubscriptions().stream())
            .filter(subscription -> destination.equals(subscription.getDestination()))
            .count();
    }
}
