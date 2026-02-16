package in.koreatech.koin.domain.callvan.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.domain.callvan.model.CallvanNotification;
import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.domain.callvan.model.enums.CallvanNotificationType;
import in.koreatech.koin.domain.callvan.repository.CallvanNotificationRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanPostRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CallvanNotificationScheduler {

    private final CallvanPostRepository callvanPostRepository;
    private final CallvanNotificationRepository callvanNotificationRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ZoneId systemZone = ZoneId.of("Asia/Seoul");
    private static final String NOTIFICATION_QUEUE_KEY = "callvan:notification:queue";

    public void scheduleNotification(CallvanPost post) {
        LocalDateTime departureTime = LocalDateTime.of(
            post.getDepartureDate(),
            post.getDepartureTime()
        );
        LocalDateTime notificationTime = departureTime.minusMinutes(30);
        LocalDateTime now = LocalDateTime.now();

        if (notificationTime.isBefore(now) || notificationTime.isEqual(now)) {
            log.info("콜벤팟 알림 시간이 이미 지나서 스케줄링하지 않음 - postId: {}, notificationTime: {}",
                post.getId(), notificationTime
            );
            return;
        }

        long score = notificationTime.atZone(systemZone).toEpochSecond();

        CallvanNotificationTask task = CallvanNotificationTask.builder()
            .postId(post.getId())
            .type(CallvanNotificationType.DEPARTURE_UPCOMING)
            .build();

        try {
            String taskJson = objectMapper.writeValueAsString(task);
            redisTemplate.opsForZSet().add(NOTIFICATION_QUEUE_KEY, taskJson, score);
        } catch (JsonProcessingException e) {
            log.info("콜벤팟 알림 작업 생성 실패 : {}", post.getId(), e);
        }
    }

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void processScheduledNotifications() {
        long now = ZonedDateTime.now(systemZone).toEpochSecond();

        Set<String> tasks = redisTemplate.opsForZSet()
            .rangeByScore(NOTIFICATION_QUEUE_KEY, 0, now);

        if (tasks == null || tasks.isEmpty()) {
            return;
        }

        for (String taskJson : tasks) {
            try {
                CallvanNotificationTask task = objectMapper.readValue(taskJson, CallvanNotificationTask.class);
                processNotification(task);

                redisTemplate.opsForZSet().remove(NOTIFICATION_QUEUE_KEY, taskJson);
            } catch (Exception e) {
                log.info("콜벤팟 알림 작업 처리 실패 : {}", e.getMessage());
            }
        }
    }

    private void processNotification(CallvanNotificationTask task) {
        CallvanPost post = callvanPostRepository.findById(task.getPostId())
            .orElse(null);

        if (post == null || post.getIsDeleted()) {
            return;
        }

        List<CallvanNotification> notifications = post.getParticipants().stream()
            .filter(p -> !p.getIsDeleted())
            .map(participant -> CallvanNotification.builder()
                .recipient(participant.getMember())
                .notificationType(CallvanNotificationType.DEPARTURE_UPCOMING)
                .post(post)
                .departureType(post.getDepartureType())
                .departureCustomName(post.getDepartureCustomName())
                .arrivalType(post.getArrivalType())
                .arrivalCustomName(post.getArrivalCustomName())
                .departureDate(post.getDepartureDate())
                .departureTime(post.getDepartureTime())
                .currentParticipants(post.getCurrentParticipants())
                .maxParticipants(post.getMaxParticipants())
                .build())
            .toList();

        callvanNotificationRepository.saveAll(notifications);
    }

    @Builder
    @Getter
    private static class CallvanNotificationTask {
        private Integer postId;
        private CallvanNotificationType type;
    }
}
