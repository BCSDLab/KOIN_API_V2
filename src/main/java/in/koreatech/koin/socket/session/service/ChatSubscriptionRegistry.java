package in.koreatech.koin.socket.session.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.broker.DefaultSubscriptionRegistry;
import org.springframework.messaging.support.MessageBuilder;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChatSubscriptionRegistry extends DefaultSubscriptionRegistry {

    private final ChatSocketSessionRegistry sessions;
    // 현재 broker에 등록된 세대만 보관한다. 과거 구독 ID를 누적하지 않는다.
    private final Map<String, Map<String, Long>> registered = new ConcurrentHashMap<>();

    @Override
    protected void addSubscriptionInternal(String sessionId, String subscriptionId, String destination,
        Message<?> message) {
        Long generation = message.getHeaders().get(ChatSocketSessionRegistry.SUBSCRIPTION_GENERATION, Long.class);
        sessions.registerBrokerSubscription(sessionId, subscriptionId, generation, () -> {
            Map<String, Long> subscriptions = registered.computeIfAbsent(sessionId, ignored -> new ConcurrentHashMap<>());
            // 이전 UNSUBSCRIBE가 늦게 도착해도 새 목적지 등록을 막지 않도록 먼저 이전 등록을 교체한다.
            if (subscriptions.containsKey(subscriptionId)) {
                super.removeSubscriptionInternal(sessionId, subscriptionId, message);
            }
            super.addSubscriptionInternal(sessionId, subscriptionId, destination, message);
            subscriptions.put(subscriptionId, generation);
        });
    }

    @Override
    protected void removeSubscriptionInternal(String sessionId, String subscriptionId, Message<?> message) {
        Long generation = message.getHeaders().get(ChatSocketSessionRegistry.SUBSCRIPTION_GENERATION, Long.class);
        if (generation == null) {
            return;
        }
        sessions.withConnectionLock(sessionId, () -> {
            Map<String, Long> subscriptions = registered.get(sessionId);
            if (subscriptions != null && generation.equals(subscriptions.get(subscriptionId))) {
                super.removeSubscriptionInternal(sessionId, subscriptionId, message);
                subscriptions.remove(subscriptionId);
                if (subscriptions.isEmpty()) {
                    registered.remove(sessionId, subscriptions);
                }
            }
        });
    }

    @Override
    public void unregisterAllSubscriptions(String sessionId) {
        sessions.withConnectionLock(sessionId, () -> {
            // broker의 heartbeat 종료도 이후 대기 중인 SUBSCRIBE에 대해 최종 종료로 처리한다.
            sessions.disconnect(sessionId);
            super.unregisterAllSubscriptions(sessionId);
            registered.remove(sessionId);
        });
    }

    @EventListener
    public void revoked(ChatSocketSessionRegistry.SubscriptionRevoked event) {
        var headers = SimpMessageHeaderAccessor.create(SimpMessageType.UNSUBSCRIBE);
        headers.setSessionId(event.sessionId());
        headers.setSubscriptionId(event.subscriptionId());
        headers.setHeader(ChatSocketSessionRegistry.SUBSCRIPTION_GENERATION, event.generation());
        unregisterSubscription(MessageBuilder.createMessage(new byte[0], headers.getMessageHeaders()));
    }
}
