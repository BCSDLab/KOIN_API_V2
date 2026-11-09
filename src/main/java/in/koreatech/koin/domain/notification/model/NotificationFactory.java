package in.koreatech.koin.domain.notification.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.notification.i18n.NotificationMessages;
import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.common.model.MobileAppPath;

@Component
public class NotificationFactory {

    public Notification generateOrderNotification(
        MobileAppPath path,
        Integer orderId,
        String shopName,
        String status,
        boolean delivery,
        LocalDateTime estimatedAt,
        User target
    ) {
        return new Notification(
            path,
            generateSchemeUri(path, orderId),
            NotificationMessages.translateKnown(target, generateOrderTitle(status)),
            generateOrderMessage(status, shopName, delivery, estimatedAt, target),
            null,
            NotificationType.MESSAGE,
            target
        );
    }

    public Notification generateClubRecruitmentNotification(
        MobileAppPath path,
        Integer clubId,
        String clubName,
        User target
    ) {
        return new Notification(
            path,
            generateClubRecruitmentSchemeUri(path, clubId),
            NotificationMessages.format(target, "[코인동아리] %s 모집 공고가 갱신되었어요!", clubName),
            NotificationMessages.format(target, "%s에 모집 정보가 새로 업데이트되었어요.\n행사 내용 둘러보기", clubName),
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
            NotificationMessages.format(target, "[코인 동아리] %s에 내일 예정된 행사가 있어요!", clubName),
            NotificationMessages.format(target, "%s - %s의 행사가 %s에 진행 될 예정이에요.\n행사 내용 둘러보기",
                eventName, clubName, formatEventDateTime(eventDateTime, target)),
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
            NotificationMessages.format(target, "[코인 동아리] 오늘 %s 행사가 한 시간 남았어요!", clubName),
            NotificationMessages.format(target, "%s - %s의 행사가 1시간 뒤에 시작 할 예정이에요.\n행사 내용 둘러보기", eventName, clubName),
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
            String.format("%s%s", shopName, NotificationMessages.translateKnown(target, title)),
            NotificationMessages.translateKnown(target, message),
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
            NotificationMessages.format(target, "%s의 이벤트가 추가되었어요 🎉", shopName),
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
            NotificationMessages.format(target, "%s 품절됐어요 😢",
                isKorean(target) ? getPostposition(place, "이", "가") : place),
            NotificationMessages.format(target, "다른 코너 메뉴도 확인해보세요"),
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
            NotificationMessages.format(target, "식단 사진이 업로드 됐어요!"),
            NotificationMessages.format(target, "사진 보러 가기 😋"),
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
        User target
    ) {
        return new Notification(
            path,
            generateKeywordSchemeUri(path, eventKeywordId, keyword, boardId),
            title,
            NotificationMessages.format(target, "방금 등록된 %s 공지를 확인해보세요!", keyword),
            null,
            NotificationType.MESSAGE,
            target
        );
    }

    public Notification generateLostItemKeywordNotification(
        MobileAppPath path,
        Integer eventKeywordId,
        String keyword,
        String title,
        User target
    ) {
        return new Notification(
            path,
            generateLostItemKeywordSchemeUri(path, eventKeywordId, keyword),
            title,
            NotificationMessages.format(target, "방금 등록된 %s 분실물 게시물을 확인해보세요!", keyword),
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
            NotificationMessages.format(target, "%s님의 메시지", senderName),
            messageContent,
            null,
            NotificationType.MESSAGE,
            target
        );
    }

    private String generateOrderTitle(String status) {
        return switch (status) {
            case "COOKING" -> "주문이 접수되었어요";
            case "DELIVERING" -> "배달이 시작됐어요";
            case "PACKAGED" -> "포장이 완료됐어요";
            case "PICKED_UP" -> "주문 수령이 완료됐어요";
            case "DELIVERED" -> "배달이 완료됐어요";
            case "CANCELED" -> "주문이 취소됐어요";
            default -> throw new IllegalArgumentException("알림 대상이 아닌 주문 상태: " + status);
        };
    }

    private String generateOrderMessage(String status, String shopName, boolean delivery, LocalDateTime estimatedAt, User target) {
        return switch (status) {
            case "COOKING" -> appendEstimatedTime(
                NotificationMessages.format(target, "%s에서 조리를 시작했어요.", shopName),
                delivery ? "예상 도착" : "포장 완료 예정",
                estimatedAt, target
            );
            case "DELIVERING" -> appendEstimatedTime(
                NotificationMessages.format(target, "%s에서 주문하신 음식이 배달 중이에요.", shopName), "예상 도착", estimatedAt, target
            );
            case "PACKAGED" -> NotificationMessages.format(target, "%s에서 주문을 수령해 주세요.", shopName);
            case "PICKED_UP", "DELIVERED" -> NotificationMessages.format(target, "%s 주문, 맛있게 드세요!", shopName);
            case "CANCELED" -> NotificationMessages.format(target, "%s 주문의 자세한 내용은 주문 내역을 확인해 주세요.", shopName);
            default -> throw new IllegalArgumentException("알림 대상이 아닌 주문 상태: " + status);
        };
    }

    private String appendEstimatedTime(String message, String estimatedTimeLabel, LocalDateTime estimatedAt, User target) {
        String estimatedTime = estimatedAt.format(DateTimeFormatter.ofPattern(isKorean(target) ? "M월 d일 HH:mm" : "MM-dd HH:mm"));
        return "%s\n%s: %s".formatted(message, NotificationMessages.translateKnown(target, estimatedTimeLabel), estimatedTime);
    }

    private String formatEventDateTime(LocalDateTime dateTime, User target) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(isKorean(target) ? "M월 d일 HH시 mm분" : "MM-dd HH:mm");
        return dateTime.format(formatter);
    }

    private boolean isKorean(User target) {
        return SupportedLanguage.fromStoredCode(
            target == null ? null : target.getPreferredLanguage())
            == SupportedLanguage.KOREAN;
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

    private String generateLostItemKeywordSchemeUri(MobileAppPath path, Integer eventId, String keyword) {
        if (keyword == null) {
            return generateSchemeUri(path, eventId);
        }
        return String.format("%s?id=%d&keyword=%s", path.getPath(), eventId, keyword);
    }

    private String generateChatMessageSchemeUri(MobileAppPath path, Integer articleId, Integer chatRoomId) {
        if (chatRoomId == null) {
            return generateSchemeUri(path, articleId);
        }
        return String.format("%s?articleId=%d&chatRoomId=%d", path.getPath(), articleId, chatRoomId);
    }
}
