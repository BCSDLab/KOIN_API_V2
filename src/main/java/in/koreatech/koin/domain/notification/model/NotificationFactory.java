package in.koreatech.koin.domain.notification.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.common.model.MobileAppPath;

@Component
public class NotificationFactory {

    public Notification generateClubRecruitmentNotification(
        MobileAppPath path,
        Integer clubId,
        String clubName,
        User target
    ) {
        return new Notification(
            path,
            generateClubRecruitmentSchemeUri(path, clubId),
            "[코인동아리] %s 모집 공고가 갱신되었어요!".formatted(clubName),
            "%s에 모집 정보가 새로 업데이트되었어요.\n행사 내용 둘러보기".formatted(clubName),
            null,
            NotificationType.MESSAGE,
            target
        );
    }

    public Notification generateClubEventNotificationBeforeOneDay(
        MobileAppPath path,
        Integer clubId,
        Integer eventId,
        String eventName,
        String clubName,
        LocalDateTime eventDateTime,
        User target
    ) {
        return new Notification(
            path,
            generateClubEventSchemeUri(path, clubId, eventId),
            "[코인 동아리] %s에 내일 예정된 행사가 있어요!".formatted(clubName),
            "%s - %s의 행사가 %s에 진행 될 예정이에요.\n행사 내용 둘러보기"
                .formatted(eventName, clubName, formatEventDateTime(eventDateTime)),
            null,
            NotificationType.MESSAGE,
            target
        );
    }

    public Notification generateClubEventNotificationBeforeOneHour(
        MobileAppPath path,
        Integer clubId,
        Integer eventId,
        String eventName,
        String clubName,
        User target
    ) {
        return new Notification(
            path,
            generateClubEventSchemeUri(path, clubId, eventId), // 검토필요
            "[코인 동아리] 오늘 %s 행사가 한 시간 남았어요!".formatted(clubName),
            "%s - %s의 행사가 1시간 뒤에 시작 할 예정이에요.\n행사 내용 둘러보기".formatted(eventName, clubName),
            null,
            NotificationType.MESSAGE,
            target
        );
    }

    public Notification generateReviewPromptNotification(
        MobileAppPath path,
        Integer eventShopId,
        String shopName,
        String title,
        String message,
        User target
    ) {
        return new Notification(
            path,
            generateSchemeUri(path, eventShopId),
            String.format("%s%s", shopName, title),
            message,
            null,
            NotificationType.MESSAGE,
            target
        );
    }

    public Notification generateShopEventCreateNotification(
        MobileAppPath path,
        Integer eventShopId,
        String imageUrl,
        String shopName,
        String title,
        User target
    ) {
        return new Notification(
            path,
            generateSchemeUri(path, eventShopId),
            "%s의 이벤트가 추가되었어요 🎉".formatted(shopName),
            "%s".formatted(title),
            imageUrl,
            NotificationType.MESSAGE,
            target
        );
    }

    public Notification generateSoldOutNotification(
        MobileAppPath path,
        Integer eventDiningId,
        String place,
        User target
    ) {
        return new Notification(
            path,
            generateSchemeUri(path, eventDiningId),
            "%s 품절됐어요 \uD83D\uDE22".formatted(getPostposition(place, "이", "가")),
            "다른 코너 메뉴도 확인해보세요",
            null,
            NotificationType.MESSAGE,
            target
        );
    }

    public Notification generateDiningImageUploadNotification(
        MobileAppPath path,
        Integer eventDiningId,
        String imageUrl,
        User target
    ) {
        return new Notification(
            path,
            generateSchemeUri(path, eventDiningId),
            "식단 사진이 업로드 됐어요!",
            "사진 보러 가기 \uD83D\uDE0B",
            imageUrl,
            NotificationType.MESSAGE,
            target
        );
    }

    public Notification generateKeywordNotification(
        MobileAppPath path,
        Integer eventKeywordId,
        String keyword,
        String title,
        Integer boardId,
        String description,
        User target
    ) {
        return new Notification(
            path,
            generateKeywordSchemeUri(path, eventKeywordId, keyword, boardId),
            title,
            description,
            null,
            NotificationType.MESSAGE,
            target
        );
    }

    public Notification generateChatMessageNotification(
        MobileAppPath path,
        Integer articleId,
        Integer chatRoomId,
        String senderName,
        String messageContent,
        User target
    ) {
        return new Notification(
            path,
            generateChatMessageSchemeUri(path, articleId, chatRoomId),
            String.format("%s님의 메시지", senderName),
            messageContent,
            null,
            NotificationType.MESSAGE,
            target
        );
    }

    private String formatEventDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M월 d일 HH시 mm분");
        return dateTime.format(formatter);
    }

    private String generateClubRecruitmentSchemeUri(MobileAppPath path, Integer clubId) {
        return String.format("%s-recruitment?id=%d", path.getPath(), clubId);
    }

    private String generateSchemeUri(MobileAppPath path, Integer eventId) {
        if (eventId == null) {
            return path.getPath();
        }
        return String.format("%s?id=%d", path.getPath(), eventId);
    }

    private String generateClubEventSchemeUri(MobileAppPath path, Integer clubId, Integer eventId) {
        if (eventId == null) {
            return generateSchemeUri(path, clubId);
        }
        return String.format("%s?clubId=%d&eventId=%d", path.getPath(), clubId, eventId);
    }

    private String getPostposition(String place, String firstPost, String secondPost) {
        char lastChar = place.charAt(place.length() - 1);
        String result = (lastChar - 0xAC00) % 28 > 0 ? firstPost : secondPost;
        return place + result;
    }

    private String generateKeywordSchemeUri(MobileAppPath path, Integer eventId, String keyword, Integer boardId) {
        if (keyword == null) {
            return generateSchemeUri(path, eventId);
        }
        return String.format("%s?id=%d&keyword=%s&board-id=%s", path.getPath(), eventId, keyword, boardId);
    }

    private String generateChatMessageSchemeUri(MobileAppPath path, Integer articleId, Integer chatRoomId) {
        if (chatRoomId == null) {
            return generateSchemeUri(path, articleId);
        }
        return String.format("%s?articleId=%d&chatRoomId=%d", path.getPath(), articleId, chatRoomId);
    }
}
