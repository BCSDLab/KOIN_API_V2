package in.koreatech.koin.domain.notification.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.notification.repository.NotificationRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationDeviceTokenCleanupService {

    private final Clock clock;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void cleanupYesterdayUnregisteredDeviceTokens() {
        LocalDate today = LocalDate.now(clock);
        LocalDateTime start = today.minusDays(1).atStartOfDay();
        LocalDateTime end = today.atStartOfDay();

        List<Integer> userIds = notificationRepository.findUnregisteredPushFailureUserIds(start, end);
        log.info("전날 UNREGISTERED 실패 알림 기준 deviceToken 삭제 대상 조회. start={}, end={}, count={}",
            start,
            end,
            userIds.size()
        );

        if (!userIds.isEmpty()) {
            userRepository.clearDeviceTokensByIdIn(userIds);
        }
        log.info("deviceToken 삭제 요청 완료. count={}", userIds.size());
    }
}
