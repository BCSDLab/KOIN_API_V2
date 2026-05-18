package in.koreatech.koin.domain.community.article.service.summary;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ArticleSummaryValidator {

    private static final int MAX_ITEMS = 3;
    private static final int MAX_TEXT_LENGTH = 80;
    private static final Pattern CRITICAL_TOKEN_PATTERN = Pattern.compile(
        "(\\d{4}[./-]\\d{1,2}[./-]\\d{1,2})"
            + "|(\\d{1,2}\\s*월\\s*\\d{1,2}\\s*일)"
            + "|(\\d{1,2}\\s*시(?:\\s*\\d{1,2}\\s*분)?)"
            + "|(\\d[\\d,]*(?:원|만원|명|개|학점))"
    );

    public List<String> validate(ArticleSummaryResult result, String sourceText) {
        if (result == null || result.items() == null || result.items().isEmpty()) {
            throw new ArticleSummaryValidationException("요약할 핵심 정보가 없습니다.");
        }
        if (hasTooManyItems(result)) {
            throw new ArticleSummaryValidationException("요약 항목은 최대 3개까지 허용됩니다.");
        }

        Set<String> seen = new HashSet<>();
        String normalizedSource = normalizeForComparison(sourceText);
        return result.items().stream()
            .map(item -> validateItem(item, normalizedSource, seen))
            .toList();
    }

    public boolean hasTooManyItems(ArticleSummaryResult result) {
        return result != null && result.items() != null && result.items().size() > MAX_ITEMS;
    }

    public int maxItems() {
        return MAX_ITEMS;
    }

    private String validateItem(ArticleSummaryItem item, String normalizedSource, Set<String> seen) {
        if (item == null) {
            throw new ArticleSummaryValidationException("요약 항목이 비어 있습니다.");
        }
        String text = removeEmoji(item.text()).trim();
        if (!StringUtils.hasText(text)) {
            throw new ArticleSummaryValidationException("요약 문장이 비어 있습니다.");
        }
        if (text.length() > MAX_TEXT_LENGTH) {
            throw new ArticleSummaryValidationException("요약 문장이 80자를 초과했습니다.");
        }
        if (isMeaningless(text)) {
            throw new ArticleSummaryValidationException("구체 정보가 없는 요약 문장입니다.");
        }
        if (!seen.add(normalizeForComparison(text))) {
            throw new ArticleSummaryValidationException("요약 문장이 중복되었습니다.");
        }
        validateCriticalTokens(text, normalizedSource);
        return new ArticleSummaryItem(item.icon(), text).formattedLine();
    }

    private void validateCriticalTokens(String text, String normalizedSource) {
        Matcher matcher = CRITICAL_TOKEN_PATTERN.matcher(text);
        while (matcher.find()) {
            String token = normalizeForComparison(matcher.group());
            if (!normalizedSource.contains(token)) {
                throw new ArticleSummaryValidationException("출처에 없는 날짜/숫자 정보가 포함되었습니다.");
            }
        }
    }

    private boolean isMeaningless(String text) {
        String normalized = normalizeForComparison(text);
        if (normalized.contains("자세한내용은확인") || normalized.contains("자세한사항은확인")) {
            return true;
        }
        return normalized.contains("첨부문서확인")
            || normalized.contains("첨부파일확인")
            || normalized.contains("첨부자료확인")
            || normalized.contains("첨부문서참고")
            || normalized.contains("첨부파일참고")
            || normalized.contains("첨부자료참고");
    }

    private String removeEmoji(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        text.codePoints()
            .filter(codePoint -> Character.getType(codePoint) != Character.OTHER_SYMBOL)
            .forEach(builder::appendCodePoint);
        return builder.toString();
    }

    private String normalizeForComparison(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT)
            .replaceAll("\\s+", "")
            .replaceAll("[,._\\-/:()\\[\\]{}]", "");
    }
}
