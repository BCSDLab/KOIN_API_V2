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
    private static final int MAX_TEXT_LENGTH = 260;
    private static final Pattern CRITICAL_TOKEN_PATTERN = Pattern.compile(
        "(\\d{4}[./-]\\s*\\d{1,2}[./-]\\s*\\d{1,2})"
            + "|(\\d{2}[./-]\\s*\\d{1,2}[./-]\\s*\\d{1,2})"
            + "|(\\d{1,2}\\s*월\\s*\\d{1,2}\\s*일)"
            + "|(\\d{1,2}\\s*[./]\\s*\\d{1,2})"
            + "|(\\d{1,2}\\s*(?::\\s*\\d{1,2}|시(?:\\s*\\d{1,2}\\s*분)?))"
            + "|(\\d[\\d,]*\\s*(?:원|만원|명|개|학점))"
    );
    private static final Pattern DATE_PATTERN = Pattern.compile("^(\\d{4})[./-](\\d{1,2})[./-](\\d{1,2})$");
    private static final Pattern SHORT_DATE_PATTERN = Pattern.compile("^(\\d{2})[./-](\\d{1,2})[./-](\\d{1,2})$");
    private static final Pattern MONTH_DAY_PATTERN = Pattern.compile("^(\\d{1,2})월(\\d{1,2})일$");
    private static final Pattern NUMERIC_MONTH_DAY_PATTERN = Pattern.compile("^(\\d{1,2})[./](\\d{1,2})$");
    private static final Pattern COLON_TIME_PATTERN = Pattern.compile("^(\\d{1,2}):(\\d{1,2})$");
    private static final Pattern KOREAN_TIME_PATTERN = Pattern.compile("^(\\d{1,2})시(?:(\\d{1,2})분)?$");
    private static final Pattern NUMBER_UNIT_PATTERN = Pattern.compile("^(\\d+)(만원|원|명|개|학점)$");
    private static final Pattern CANONICAL_DATE_PATTERN = Pattern.compile("^date:\\d{4}-(\\d{1,2})-(\\d{1,2})$");

    public List<String> validate(ArticleSummaryResult result, String sourceText) {
        if (result == null || result.items() == null || result.items().isEmpty()) {
            throw new ArticleSummaryValidationException("요약할 핵심 정보가 없습니다.");
        }
        if (hasTooManyItems(result)) {
            throw new ArticleSummaryValidationException("요약 항목은 최대 3개까지 허용됩니다.");
        }

        Set<String> seen = new HashSet<>();
        String normalizedSource = normalizeForComparison(sourceText);
        Set<String> sourceCriticalTokens = extractCriticalTokens(sourceText);
        return result.items().stream()
            .map(item -> validateItem(item, normalizedSource, sourceCriticalTokens, seen))
            .toList();
    }

    public boolean hasTooManyItems(ArticleSummaryResult result) {
        return result != null && result.items() != null && result.items().size() > MAX_ITEMS;
    }

    public int maxItems() {
        return MAX_ITEMS;
    }

    private String validateItem(
        ArticleSummaryItem item,
        String normalizedSource,
        Set<String> sourceCriticalTokens,
        Set<String> seen
    ) {
        if (item == null) {
            throw new ArticleSummaryValidationException("요약 항목이 비어 있습니다.");
        }
        String text = removeEmoji(item.text()).trim();
        if (!StringUtils.hasText(text)) {
            throw new ArticleSummaryValidationException("요약 문장이 비어 있습니다.");
        }
        if (text.length() > MAX_TEXT_LENGTH) {
            throw new ArticleSummaryValidationException("요약 문장이 260자를 초과했습니다.");
        }
        if (isMeaningless(text)) {
            throw new ArticleSummaryValidationException("구체 정보가 없는 요약 문장입니다.");
        }
        if (!seen.add(normalizeForComparison(text))) {
            throw new ArticleSummaryValidationException("요약 문장이 중복되었습니다.");
        }
        validateCriticalTokens(text, normalizedSource, sourceCriticalTokens);
        return new ArticleSummaryItem(item.icon(), text).formattedLine();
    }

    private void validateCriticalTokens(String text, String normalizedSource, Set<String> sourceCriticalTokens) {
        Matcher matcher = CRITICAL_TOKEN_PATTERN.matcher(text);
        while (matcher.find()) {
            String rawToken = matcher.group();
            String token = normalizeForComparison(rawToken);
            String canonicalToken = normalizeCriticalToken(rawToken);
            if (!normalizedSource.contains(token) && !sourceCriticalTokens.contains(canonicalToken)) {
                throw new ArticleSummaryValidationException("출처에 없는 날짜/숫자 정보가 포함되었습니다.");
            }
        }
    }

    private Set<String> extractCriticalTokens(String sourceText) {
        Set<String> tokens = new HashSet<>();
        Matcher matcher = CRITICAL_TOKEN_PATTERN.matcher(sourceText == null ? "" : sourceText);
        while (matcher.find()) {
            addCriticalToken(tokens, matcher.group());
        }
        return tokens;
    }

    private void addCriticalToken(Set<String> tokens, String rawToken) {
        String canonicalToken = normalizeCriticalToken(rawToken);
        tokens.add(canonicalToken);
        Matcher canonicalDateMatcher = CANONICAL_DATE_PATTERN.matcher(canonicalToken);
        if (canonicalDateMatcher.matches()) {
            tokens.add("month-day:%d-%d".formatted(
                Integer.parseInt(canonicalDateMatcher.group(1)),
                Integer.parseInt(canonicalDateMatcher.group(2))
            ));
        }
        if (canonicalToken.matches("^time:\\d{1,2}:0$")) {
            tokens.add(canonicalToken.substring(0, canonicalToken.lastIndexOf(':')));
        }
    }

    private String normalizeCriticalToken(String rawToken) {
        String value = rawToken == null ? "" : rawToken.replaceAll("\\s+", "").replace(",", "");
        Matcher dateMatcher = DATE_PATTERN.matcher(value);
        if (dateMatcher.matches()) {
            return "date:%s-%d-%d".formatted(
                dateMatcher.group(1),
                Integer.parseInt(dateMatcher.group(2)),
                Integer.parseInt(dateMatcher.group(3))
            );
        }
        Matcher shortDateMatcher = SHORT_DATE_PATTERN.matcher(value);
        if (shortDateMatcher.matches()) {
            return "date:%d-%d-%d".formatted(
                2000 + Integer.parseInt(shortDateMatcher.group(1)),
                Integer.parseInt(shortDateMatcher.group(2)),
                Integer.parseInt(shortDateMatcher.group(3))
            );
        }
        Matcher monthDayMatcher = MONTH_DAY_PATTERN.matcher(value);
        if (monthDayMatcher.matches()) {
            return "month-day:%d-%d".formatted(
                Integer.parseInt(monthDayMatcher.group(1)),
                Integer.parseInt(monthDayMatcher.group(2))
            );
        }
        Matcher numericMonthDayMatcher = NUMERIC_MONTH_DAY_PATTERN.matcher(value);
        if (numericMonthDayMatcher.matches()) {
            return "month-day:%d-%d".formatted(
                Integer.parseInt(numericMonthDayMatcher.group(1)),
                Integer.parseInt(numericMonthDayMatcher.group(2))
            );
        }
        Matcher colonTimeMatcher = COLON_TIME_PATTERN.matcher(value);
        if (colonTimeMatcher.matches()) {
            return "time:%d:%d".formatted(
                Integer.parseInt(colonTimeMatcher.group(1)),
                Integer.parseInt(colonTimeMatcher.group(2))
            );
        }
        Matcher koreanTimeMatcher = KOREAN_TIME_PATTERN.matcher(value);
        if (koreanTimeMatcher.matches()) {
            if (koreanTimeMatcher.group(2) == null) {
                return "time:%d".formatted(Integer.parseInt(koreanTimeMatcher.group(1)));
            }
            return "time:%d:%d".formatted(
                Integer.parseInt(koreanTimeMatcher.group(1)),
                Integer.parseInt(koreanTimeMatcher.group(2))
            );
        }
        Matcher numberUnitMatcher = NUMBER_UNIT_PATTERN.matcher(value);
        if (numberUnitMatcher.matches()) {
            return "number:%d%s".formatted(
                Long.parseLong(numberUnitMatcher.group(1)),
                numberUnitMatcher.group(2)
            );
        }
        return normalizeForComparison(rawToken);
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
