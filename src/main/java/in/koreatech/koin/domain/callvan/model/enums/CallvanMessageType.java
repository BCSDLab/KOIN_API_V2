package in.koreatech.koin.domain.callvan.model.enums;

public enum CallvanMessageType {
    TEXT {
        @Override
        public String toNotificationContent(String senderNickname, String messageContent) {
            return truncate(messageContent);
        }
    },
    IMAGE {
        @Override
        public String toNotificationContent(String senderNickname, String messageContent) {
            return senderNickname + "님이 사진을 보냈습니다.";
        }
    };

    private static final int MAX_LENGTH = 95;

    public abstract String toNotificationContent(String senderNickname, String messageContent);

    private static String truncate(String content) {
        if (content == null || content.length() <= MAX_LENGTH) {
            return content;
        }
        return content.substring(0, MAX_LENGTH) + "...";
    }
}
