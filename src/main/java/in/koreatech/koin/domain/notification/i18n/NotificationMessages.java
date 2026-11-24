package in.koreatech.koin.domain.notification.i18n;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;

import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType;

/** 언어별 properties의 시스템 알림 문구를 사용한다. 사용자 작성 내용은 번역하지 않는다. */
public final class NotificationMessages {
    private static final Map<SupportedLanguage, Properties> MESSAGES = loadMessages();
    private static final Map<String, String> KEYS_BY_KOREAN = MESSAGES.get(SupportedLanguage.KOREAN)
        .stringPropertyNames().stream()
        .collect(Collectors.toUnmodifiableMap(key -> MESSAGES.get(SupportedLanguage.KOREAN).getProperty(key), key -> key));

    private NotificationMessages() {}

    public static String format(User user, String koreanTemplate, Object... arguments) {
        return template(user, koreanTemplate).formatted(arguments);
    }

    /** Exact fixed copy only. Dynamic values must be passed separately to format. */
    public static String translateKnown(User user, String text) {
        return text == null || text.contains("%s") ? text : template(user, text);
    }

    /** 저장 당시 제목을 우선 사용하며, 구형 알림은 모호하지 않은 템플릿만 복원한다. */
    public static String teamMessage(User user,
        TeamRecruitmentNotificationType type,
        String recruitmentTitle, String original, String decisionReason) {
        if (type == null || original == null) {
            return original;
        }
        List<String> keys = switch (type) {
            case NEW_APPLICATION -> List.of("team.application.new");
            case APPLICATION_ACCEPTED -> List.of("team.application.accepted");
            case APPLICATION_REJECTED -> List.of("team.application.rejected",
                "team.application.rejected-closed", "team.application.rejected-deleted");
            case RECRUITMENT_CLOSED -> List.of("team.recruitment.closed");
            case RECRUITMENT_DELETED -> List.of("team.recruitment.deleted");
            case NEW_CHAT_MESSAGE -> List.of();
        };
        if (recruitmentTitle == null && type == TeamRecruitmentNotificationType.APPLICATION_REJECTED) {
            // 구형 알림의 현재 모집글 제목은 증거가 아니다. 저장된 지원 결정 사유만 사용한다.
            keys = switch (decisionReason == null ? "" : decisionReason) {
                case "APPLICATION_REJECTED" -> List.of("team.application.rejected");
                case "RECRUITMENT_CLOSED" -> List.of("team.application.rejected-closed");
                case "RECRUITMENT_DELETED" -> List.of("team.application.rejected-deleted");
                default -> keys;
            };
        }
        List<String> templates = keys.stream()
            .map(key -> MESSAGES.get(SupportedLanguage.KOREAN).getProperty(key))
            .toList();
        String exactTemplate = templates.stream()
            .filter(template -> template.formatted(recruitmentTitle).equals(original))
            .findFirst()
            .orElse(null);
        if (recruitmentTitle != null && exactTemplate != null) {
            return format(user, exactTemplate, recruitmentTitle);
        }

        String translated = null;
        for (String template : templates) {
            int placeholder = template.indexOf("%s");
            String prefix = template.substring(0, placeholder);
            String suffix = template.substring(placeholder + 2);
            if (!original.startsWith(prefix) || !original.endsWith(suffix)
                || original.length() <= prefix.length() + suffix.length()) {
                continue;
            }
            // 거절 사유와 사용자 제목을 구별할 수 없는 구형 알림은 추측하지 않는다.
            if (translated != null) {
                return original;
            }
            String originalTitle = original.substring(prefix.length(), original.length() - suffix.length());
            translated = format(user, template, originalTitle);
        }
        return translated == null ? original : translated;
    }

    private static String template(User user, String koreanTemplate) {
        String key = KEYS_BY_KOREAN.get(koreanTemplate);
        if (key == null) {
            return koreanTemplate;
        }
        SupportedLanguage language = SupportedLanguage.fromStoredCode(user == null ? null : user.getPreferredLanguage());
        return MESSAGES.get(language).getProperty(key, koreanTemplate);
    }

    private static Map<SupportedLanguage, Properties> loadMessages() {
        Map<SupportedLanguage, Properties> messages = new EnumMap<>(SupportedLanguage.class);
        for (SupportedLanguage language : SupportedLanguage.values()) {
            String path = "i18n/notifications/" + language.getCode() + ".properties";
            try (var reader = new InputStreamReader(new ClassPathResource(path).getInputStream(), StandardCharsets.UTF_8)) {
                Properties properties = new Properties();
                properties.load(reader);
                messages.put(language, properties);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot load notification messages: " + path, e);
            }
        }
        return Map.copyOf(messages);
    }
}
