package in.koreatech.koin.domain.club.service;

import static in.koreatech.koin.common.model.MobileAppPath.CLUB;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.event.model.ClubEvent;
import in.koreatech.koin.domain.club.event.model.ClubEventSubscription;
import in.koreatech.koin.domain.club.model.ClubHot;
import in.koreatech.koin.domain.club.model.redis.ClubHotRedis;
import in.koreatech.koin.domain.club.event.repository.ClubEventRepository;
import in.koreatech.koin.domain.club.event.repository.ClubEventSubscriptionRepository;
import in.koreatech.koin.domain.club.repository.ClubHotRepository;
import in.koreatech.koin.domain.club.repository.ClubRepository;
import in.koreatech.koin.domain.club.repository.redis.ClubHitsRedisRepository;
import in.koreatech.koin.domain.club.repository.redis.ClubHotRedisRepository;
import in.koreatech.koin.domain.notification.model.Notification;
import in.koreatech.koin.domain.notification.model.NotificationFactory;
import in.koreatech.koin.domain.notification.service.NotificationService;
import in.koreatech.koin.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubScheduleService {

    private static final int KEY_PREFIX_LENGTH = "club:hits:".length();

    private final ClubRepository clubRepository;
    private final ClubHotRedisRepository hotClubRedisRepository;
    private final ClubHotRepository clubHotRepository;
    private final ClubHitsRedisRepository clubHitsRedisRepository;
    private final ClubEventRepository clubEventRepository;
    private final ClubEventSubscriptionRepository clubEventSubscriptionRepository;
    private final NotificationFactory notificationFactory;
    private final NotificationService notificationService;

    @Transactional
    public void updateHotClub() {
        List<Club> clubs = clubRepository.findAll();
        if (clubs.isEmpty()) {
            log.info("등록된 동아리가 없어 인기 동아리 갱신이 이루어지지 않았습니다.");
            return;
        }

        Club topClub = clubs.stream()
            .max(Comparator.comparingInt(Club::getHitsIncrease))
            .get();

        ClubHot clubHot = ClubHot.builder()
            .club(topClub)
            .ranking(1)
            .periodHits(topClub.getHitsIncrease())
            .startDate(getStartOfLastWeek())
            .endDate(getEndOfLastWeek())
            .build();
        clubHotRepository.save(clubHot);
        hotClubRedisRepository.save(ClubHotRedis.from(topClub));

        clubs.forEach(Club::updateLastWeekHits);
    }

    private LocalDate getStartOfLastWeek() {
        return LocalDate.now()
            .minusWeeks(1)
            .with(DayOfWeek.MONDAY);
    }

    private LocalDate getEndOfLastWeek() {
        return LocalDate.now()
            .minusWeeks(1)
            .with(DayOfWeek.SUNDAY);
    }

    @Transactional
    public void syncHitsFromRedisToDatabase() {
        Set<String> keys = clubHitsRedisRepository.getAllHitsKeys();
        if (keys.isEmpty()) return;

        Map<Integer, String> clubIdToKeyMap = new HashMap<>();
        List<Integer> clubIds = new ArrayList<>();

        for (String key : keys) {
            Integer clubId = Integer.parseInt(key.substring(KEY_PREFIX_LENGTH));
            clubIdToKeyMap.put(clubId, key);
            clubIds.add(clubId);
        }

        List<Integer> existingIds = clubRepository.findExistingIds(clubIds);
        Set<String> deletedKeys = new HashSet<>();

        for (Integer clubId : existingIds) {
            try {
                String key = clubIdToKeyMap.get(clubId);
                Integer hits = clubHitsRedisRepository.getHits(key);
                clubRepository.incrementHitsByValue(clubId, hits);
                deletedKeys.add(key);
            } catch (Exception e) {
                log.warn("clubId {}의 조회수 DB 반영 실패", clubId, e);
            }
        }

        clubHitsRedisRepository.deleteHitsByKeys(deletedKeys);
    }

    @Transactional
    public void sendClubEventNotificationsBeforeOneDay() {
        List<ClubEvent> events = getClubEventsStartingTomorrow();
        if (events.isEmpty()) return;

        Map<Integer, List<User>> eventIdToUserMap = getEventIdToUserMap(events);

        for (ClubEvent event : events) {
            List<User> subscribers = eventIdToUserMap.getOrDefault(event.getId(), List.of());
            if (subscribers.isEmpty()) continue;

            List<Notification> notifications = subscribers.stream()
                .map(user -> createClubEventNotificationBeforeOneDay(event, user))
                .toList();
            notificationService.pushNotifications(notifications);
        }
    }

    private List<ClubEvent> getClubEventsStartingTomorrow() {
        LocalDateTime start = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return clubEventRepository.findAllWithClubByStartDateBetween(start, end);
    }

    private Map<Integer, List<User>> getEventIdToUserMap(List<ClubEvent> events) {
        List<Integer> eventIds = events.stream()
            .map(ClubEvent::getId)
            .toList();

        List<ClubEventSubscription> subscriptions =
            clubEventSubscriptionRepository.findAllWithUserByEventIdIn(eventIds);

        Map<Integer, List<User>> eventIdToUserMap = new HashMap<>();
        for (ClubEventSubscription subscription : subscriptions) {
            Integer eventId = subscription.getClubEvent().getId();
            User user = subscription.getUser();
            eventIdToUserMap.computeIfAbsent(eventId, k -> new ArrayList<>()).add(user);
        }
        return eventIdToUserMap;
    }

    private Notification createClubEventNotificationBeforeOneDay(ClubEvent event, User user) {
        return notificationFactory.generateClubEventNotificationBeforeOneDay(
            CLUB,
            event.getClub().getId(),
            event.getId(),
            event.getName(),
            event.getClub().getName(),
            event.getStartDate(),
            user
        );
    }

    @Transactional
    public void sendClubEventNotificationsBeforeOneHour() {
        List<ClubEvent> events = getClubEventsUpcomingEventsWithOneHour();
        if (events.isEmpty()) return;

        Map<Integer, List<User>> eventIdToUserMap = getEventIdToUserMap(events);

        for (ClubEvent event : events) {
            List<User> subscribers = eventIdToUserMap.getOrDefault(event.getId(), List.of());
            if (subscribers.isEmpty()) continue;

            List<Notification> notifications = subscribers.stream()
                .map(user -> createClubEventNotificationBeforeOneHour(event, user))
                .toList();
            notificationService.pushNotifications(notifications);

            event.markAsNotifiedBeforeOneHour();
        }
    }

    private List<ClubEvent> getClubEventsUpcomingEventsWithOneHour() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime hourLater = now.plusHours(1);
        return clubEventRepository.findAllWithClubUpcomingEventsWithOneHour(now, hourLater);
    }

    private Notification createClubEventNotificationBeforeOneHour(ClubEvent event, User user) {
        return notificationFactory.generateClubEventNotificationBeforeOneHour(
            CLUB,
            event.getClub().getId(),
            event.getId(),
            event.getName(),
            event.getClub().getName(),
            user
        );
    }
}
