package in.koreatech.koin.domain.notification.eventlistener;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import in.koreatech.koin.common.event.DiningImageUploadEvent;
import in.koreatech.koin.common.event.DiningSoldOutEvent;
import in.koreatech.koin.domain.notification.service.CoopNotificationService;
import in.koreatech.koin.global.concurrent.exception.ConcurrencyLockException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class CoopEventListener {

    private static final long LOCK_WAIT_SECONDS = 7L;

    private final CoopNotificationService coopNotificationService;
    private final RedissonClient redissonClient;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDiningSoldOutRequest(DiningSoldOutEvent event) {
        String lockName = "lock:coop-dining-soldout-notification:" + event.place();
        RLock lock = redissonClient.getLock(lockName);
        boolean acquired;
        try {
            // lease를 지정하지 않아 FCM 전송 중에는 watchdog이 락을 유지한다.
            acquired = lock.tryLock(LOCK_WAIT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw ConcurrencyLockException.withDetail("품절 처리 커밋 후 알림 락 대기 중 인터럽트 lockName: " + lockName);
        }
        if (!acquired) {
            throw ConcurrencyLockException.withDetail("품절 처리 커밋 후 알림 락 대기 시간 초과 lockName: " + lockName);
        }
        try {
            coopNotificationService.sendDiningSoldOutNotifications(event.id(), event.place(), event.diningType());
        } finally {
            // REQUIRES_NEW 서비스 프록시의 알림 트랜잭션이 끝난 뒤 해제한다.
            lock.unlock();
        }
    }

    @TransactionalEventListener
    public void onDiningImageUploadRequest(DiningImageUploadEvent event) {
        coopNotificationService.sendDiningImageUploadNotifications(event.id(), event.imageUrl());
    }
}
