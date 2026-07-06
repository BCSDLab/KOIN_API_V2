package in.koreatech.koin.domain.community.article.service.summary;

import java.util.ArrayList;
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
    private static final int MAX_TEXT_LENGTH = 200;
    private static final int DIAGNOSTIC_ITEM_LENGTH = 120;
    private static final Pattern CRITICAL_TOKEN_PATTERN = Pattern.compile(
        "(\\d{4}\\s*년\\s*\\d{1,2}\\s*월\\s*\\d{1,2}\\s*일)"
            + "|(\\d{2}\\s*년\\s*\\d{1,2}\\s*월\\s*\\d{1,2}\\s*일)"
            + "|(\\d{4}[./-]\\s*\\d{1,2}[./-]\\s*\\d{1,2})"
            + "|(\\d{2}[./-]\\s*\\d{1,2}[./-]\\s*\\d{1,2})"
            + "|(\\d{1,2}\\s*월\\s*\\d{1,2}\\s*일)"
            + "|(\\d{1,2}\\s*[./]\\s*\\d{1,2})"
            + "|(\\d{1,2}\\s*(?::\\s*\\d{1,2}|시(?:\\s*\\d{1,2}\\s*분)?))"
            + "|(\\d[\\d,]*\\s*(?:원|만원|명|개|학점))"
    );
    private static final Pattern YEAR_MONTH_DAY_TO_DAY_RANGE_PATTERN = Pattern.compile(
        "(\\d{2,4})\\s*[./-]\\s*(\\d{1,2})\\s*[./-]\\s*(\\d{1,2})\\s*\\.?"
            + "\\s*(?:\\([^)]*\\))?\\s*(?:~|〜|～|-|–|—)\\s*(\\d{1,2})\\s*\\.?"
            + "\\s*(?:\\([^)]*\\))?"
    );
    private static final Pattern KOREAN_YEAR_MONTH_DAY_TO_DAY_RANGE_PATTERN = Pattern.compile(
        "(\\d{2,4})\\s*년\\s*(\\d{1,2})\\s*월\\s*(\\d{1,2})\\s*일"
            + "\\s*(?:\\([^)]*\\))?\\s*(?:~|〜|～|-|–|—)\\s*(\\d{1,2})\\s*일"
    );
    private static final Pattern MONTH_DAY_TO_MONTH_DAY_RANGE_PATTERN = Pattern.compile(
        "(\\d{1,2})\\s*[./]\\s*(\\d{1,2})\\s*\\.?"
            + "\\s*(?:\\([^)]*\\))?\\s*(?:~|〜|～|-|–|—)\\s*(\\d{1,2})\\s*[./]\\s*(\\d{1,2})"
    );
    private static final Pattern KOREAN_DATE_PATTERN = Pattern.compile("^(\\d{4})년(\\d{1,2})월(\\d{1,2})일$");
    private static final Pattern SHORT_KOREAN_DATE_PATTERN = Pattern.compile("^(\\d{2})년(\\d{1,2})월(\\d{1,2})일$");
    private static final Pattern DATE_PATTERN = Pattern.compile("^(\\d{4})[./-](\\d{1,2})[./-](\\d{1,2})$");
    private static final Pattern SHORT_DATE_PATTERN = Pattern.compile("^(\\d{2})[./-](\\d{1,2})[./-](\\d{1,2})$");
    private static final Pattern MONTH_DAY_PATTERN = Pattern.compile("^(\\d{1,2})월(\\d{1,2})일$");
    private static final Pattern NUMERIC_MONTH_DAY_PATTERN = Pattern.compile("^(\\d{1,2})[./](\\d{1,2})$");
    private static final Pattern COLON_TIME_PATTERN = Pattern.compile("^(\\d{1,2}):(\\d{1,2})$");
    private static final Pattern KOREAN_TIME_PATTERN = Pattern.compile("^(\\d{1,2})시(?:(\\d{1,2})분)?$");
    private static final Pattern NUMBER_UNIT_PATTERN = Pattern.compile("^(\\d+)(만원|원|명|개|학점)$");
    private static final Pattern CANONICAL_DATE_PATTERN = Pattern.compile("^date:\\d{4}-(\\d{1,2})-(\\d{1,2})$");

    public List<String> validate(ArticleSummaryResult result, String sourceText) {
        ValidationResult validationResult = validateFilteringInvalidItems(result, sourceText);
        if (validationResult.hasFailures()) {
            throw new ArticleSummaryValidationException(validationResult.firstFailureReason());
        }
        return validationResult.validLines();
    }

    public ValidationResult validateFilteringInvalidItems(ArticleSummaryResult result, String sourceText) {
        if (result == null || result.items() == null || result.items().isEmpty()) {
            throw new ArticleSummaryValidationException("요약할 핵심 정보가 없습니다.");
        }
        if (hasTooManyItems(result)) {
            throw new ArticleSummaryValidationException("요약 항목은 최대 3개까지 허용됩니다.");
        }

        Set<String> seen = new HashSet<>();
        String normalizedSource = normalizeForComparison(sourceText);
        Set<String> sourceCriticalTokens = extractCriticalTokens(sourceText);
        List<String> validLines = new ArrayList<>();
        List<String> failureReasons = new ArrayList<>();
        for (ArticleSummaryItem item : result.items()) {
            try {
                validLines.add(validateItem(item, normalizedSource, sourceCriticalTokens, seen));
            } catch (ArticleSummaryValidationException e) {
                failureReasons.add(e.getMessage());
            }
        }
        return new ValidationResult(List.copyOf(validLines), List.copyOf(failureReasons));
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
            throw new ArticleSummaryValidationException("요약 문장이 200자를 초과했습니다.");
        }
        if (isMeaningless(text)) {
            throw new ArticleSummaryValidationException("구체 정보가 없는 요약 문장입니다.");
        }
        String normalizedText = normalizeForComparison(text);
        if (seen.contains(normalizedText)) {
            throw new ArticleSummaryValidationException("요약 문장이 중복되었습니다.");
        }
        validateCriticalTokens(text, normalizedSource, sourceCriticalTokens);
        seen.add(normalizedText);
        return new ArticleSummaryItem(item.icon(), text).formattedLine();
    }

    private void validateCriticalTokens(String text, String normalizedSource, Set<String> sourceCriticalTokens) {
        Matcher matcher = CRITICAL_TOKEN_PATTERN.matcher(text);
        while (matcher.find()) {
            String rawToken = matcher.group();
            if (shouldIgnoreCriticalToken(rawToken, text, matcher.start(), matcher.end())) {
                continue;
            }
            String token = normalizeForComparison(rawToken);
            String canonicalToken = normalizeCriticalToken(rawToken);
            if (!normalizedSource.contains(token) && !sourceCriticalTokens.contains(canonicalToken)) {
                throw new ArticleSummaryValidationException(
                    "출처에 없는 날짜/숫자 정보가 포함되었습니다. token=%s, item=%s"
                        .formatted(rawToken, truncate(text, DIAGNOSTIC_ITEM_LENGTH))
                );
            }
        }
    }

    private Set<String> extractCriticalTokens(String sourceText) {
        Set<String> tokens = new HashSet<>();
        String source = sourceText == null ? "" : sourceText;
        addRangeCriticalTokens(tokens, source);
        Matcher matcher = CRITICAL_TOKEN_PATTERN.matcher(source);
        while (matcher.find()) {
            if (shouldIgnoreCriticalToken(matcher.group(), source, matcher.start(), matcher.end())) {
                continue;
            }
            addCriticalToken(tokens, matcher.group());
        }
        return tokens;
    }

    private void addRangeCriticalTokens(Set<String> tokens, String sourceText) {
        Matcher yearMonthDayToDayMatcher = YEAR_MONTH_DAY_TO_DAY_RANGE_PATTERN.matcher(sourceText);
        while (yearMonthDayToDayMatcher.find()) {
            int year = normalizeYear(yearMonthDayToDayMatcher.group(1));
            int month = Integer.parseInt(yearMonthDayToDayMatcher.group(2));
            int startDay = Integer.parseInt(yearMonthDayToDayMatcher.group(3));
            int endDay = Integer.parseInt(yearMonthDayToDayMatcher.group(4));
            addDateTokens(tokens, year, month, startDay);
            addDateTokens(tokens, year, month, endDay);
        }

        Matcher koreanYearMonthDayToDayMatcher = KOREAN_YEAR_MONTH_DAY_TO_DAY_RANGE_PATTERN.matcher(sourceText);
        while (koreanYearMonthDayToDayMatcher.find()) {
            int year = normalizeYear(koreanYearMonthDayToDayMatcher.group(1));
            int month = Integer.parseInt(koreanYearMonthDayToDayMatcher.group(2));
            int startDay = Integer.parseInt(koreanYearMonthDayToDayMatcher.group(3));
            int endDay = Integer.parseInt(koreanYearMonthDayToDayMatcher.group(4));
            addDateTokens(tokens, year, month, startDay);
            addDateTokens(tokens, year, month, endDay);
        }

        Matcher monthDayToMonthDayMatcher = MONTH_DAY_TO_MONTH_DAY_RANGE_PATTERN.matcher(sourceText);
        while (monthDayToMonthDayMatcher.find()) {
            addMonthDayToken(tokens, monthDayToMonthDayMatcher.group(1), monthDayToMonthDayMatcher.group(2));
            addMonthDayToken(tokens, monthDayToMonthDayMatcher.group(3), monthDayToMonthDayMatcher.group(4));
        }
    }

    private void addCriticalToken(Set<String> tokens, String rawToken) {
        String canonicalToken = normalizeCriticalToken(rawToken);
        if (canonicalToken.startsWith("invalid:")) {
            return;
        }
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
        Matcher koreanDateMatcher = KOREAN_DATE_PATTERN.matcher(value);
        if (koreanDateMatcher.matches()) {
            return canonicalDate(
                Integer.parseInt(koreanDateMatcher.group(1)),
                Integer.parseInt(koreanDateMatcher.group(2)),
                Integer.parseInt(koreanDateMatcher.group(3))
            );
        }
        Matcher shortKoreanDateMatcher = SHORT_KOREAN_DATE_PATTERN.matcher(value);
        if (shortKoreanDateMatcher.matches()) {
            return canonicalDate(
                2000 + Integer.parseInt(shortKoreanDateMatcher.group(1)),
                Integer.parseInt(shortKoreanDateMatcher.group(2)),
                Integer.parseInt(shortKoreanDateMatcher.group(3))
            );
        }
        Matcher dateMatcher = DATE_PATTERN.matcher(value);
        if (dateMatcher.matches()) {
            return canonicalDate(
                Integer.parseInt(dateMatcher.group(1)),
                Integer.parseInt(dateMatcher.group(2)),
                Integer.parseInt(dateMatcher.group(3))
            );
        }
        Matcher shortDateMatcher = SHORT_DATE_PATTERN.matcher(value);
        if (shortDateMatcher.matches()) {
            return canonicalDate(
                2000 + Integer.parseInt(shortDateMatcher.group(1)),
                Integer.parseInt(shortDateMatcher.group(2)),
                Integer.parseInt(shortDateMatcher.group(3))
            );
        }
        Matcher monthDayMatcher = MONTH_DAY_PATTERN.matcher(value);
        if (monthDayMatcher.matches()) {
            return canonicalMonthDay(
                Integer.parseInt(monthDayMatcher.group(1)),
                Integer.parseInt(monthDayMatcher.group(2))
            );
        }
        Matcher numericMonthDayMatcher = NUMERIC_MONTH_DAY_PATTERN.matcher(value);
        if (numericMonthDayMatcher.matches()) {
            return canonicalMonthDay(
                Integer.parseInt(numericMonthDayMatcher.group(1)),
                Integer.parseInt(numericMonthDayMatcher.group(2))
            );
        }
        Matcher colonTimeMatcher = COLON_TIME_PATTERN.matcher(value);
        if (colonTimeMatcher.matches()) {
            return canonicalTime(
                Integer.parseInt(colonTimeMatcher.group(1)),
                Integer.parseInt(colonTimeMatcher.group(2))
            );
        }
        Matcher koreanTimeMatcher = KOREAN_TIME_PATTERN.matcher(value);
        if (koreanTimeMatcher.matches()) {
            int hour = Integer.parseInt(koreanTimeMatcher.group(1));
            if (koreanTimeMatcher.group(2) == null) {
                return hour >= 0 && hour <= 24 ? "time:%d".formatted(hour) : "invalid:" + normalizeForComparison(rawToken);
            }
            return canonicalTime(
                hour,
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

    private boolean shouldIgnoreCriticalToken(String rawToken, String text, int start, int end) {
        String canonicalToken = normalizeCriticalToken(rawToken);
        if (canonicalToken.startsWith("invalid:")) {
            return true;
        }
        if (!canonicalToken.startsWith("month-day:") || rawToken.contains("월")) {
            return false;
        }
        return !hasNumericMonthDayContext(text, start, end);
    }

    private boolean hasNumericMonthDayContext(String text, int start, int end) {
        int from = Math.max(0, start - 8);
        int to = Math.min(text.length(), end + 8);
        String context = text.substring(from, to);
        return context.contains("월")
            || context.contains("일")
            || context.contains("마감")
            || context.contains("일정")
            || context.contains("기간")
            || context.contains("접수")
            || context.contains("신청")
            || context.contains("까지")
            || context.contains("부터")
            || context.contains("~")
            || context.contains("～")
            || context.contains("〜")
            || context.matches(".*\\([^)]*[월화수목금토일][^)]*\\).*");
    }

    private void addDateTokens(Set<String> tokens, int year, int month, int day) {
        String canonicalDate = canonicalDate(year, month, day);
        if (canonicalDate.startsWith("invalid:")) {
            return;
        }
        tokens.add(canonicalDate);
        tokens.add("month-day:%d-%d".formatted(month, day));
    }

    private void addMonthDayToken(Set<String> tokens, String rawMonth, String rawDay) {
        String canonicalMonthDay = canonicalMonthDay(Integer.parseInt(rawMonth), Integer.parseInt(rawDay));
        if (!canonicalMonthDay.startsWith("invalid:")) {
            tokens.add(canonicalMonthDay);
        }
    }

    private String canonicalDate(int year, int month, int day) {
        if (!isValidMonthDay(month, day)) {
            return "invalid:date";
        }
        return "date:%d-%d-%d".formatted(year, month, day);
    }

    private String canonicalMonthDay(int month, int day) {
        if (!isValidMonthDay(month, day)) {
            return "invalid:month-day";
        }
        return "month-day:%d-%d".formatted(month, day);
    }

    private String canonicalTime(int hour, int minute) {
        if (hour < 0 || hour > 24 || minute < 0 || minute > 59 || (hour == 24 && minute != 0)) {
            return "invalid:time";
        }
        return "time:%d:%d".formatted(hour, minute);
    }

    private int normalizeYear(String rawYear) {
        int year = Integer.parseInt(rawYear);
        return rawYear.length() == 2 ? 2000 + year : year;
    }

    private boolean isValidMonthDay(int month, int day) {
        return month >= 1 && month <= 12 && day >= 1 && day <= 31;
    }

    private boolean isMeaningless(String text) {
        String normalized = normalizeForComparison(text);
        if (normalized.contains("자세한내용은확인") || normalized.contains("자세한사항은확인")) {
            return true;
        }
        if (isAttachmentLocationOnlySummary(normalized)) {
            return true;
        }
        return normalized.contains("첨부문서확인")
            || normalized.contains("첨부파일확인")
            || normalized.contains("첨부자료확인")
            || normalized.contains("첨부문서참고")
            || normalized.contains("첨부파일참고")
            || normalized.contains("첨부자료참고")
            || normalized.contains("첨부문서에안내")
            || normalized.contains("첨부파일에안내")
            || normalized.contains("첨부이미지에안내")
            || normalized.contains("첨부자료에안내");
    }

    private boolean isAttachmentLocationOnlySummary(String normalized) {
        boolean referencesAttachmentLocation = normalized.contains("첨부문서에")
            || normalized.contains("첨부파일에")
            || normalized.contains("첨부이미지에")
            || normalized.contains("첨부자료에");
        return referencesAttachmentLocation && (
            normalized.contains("안내")
                || normalized.contains("나와있")
                || normalized.contains("포함되어있")
                || normalized.contains("제공되어있")
        );
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

    private String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }

    public record ValidationResult(
        List<String> validLines,
        List<String> failureReasons
    ) {

        public boolean hasFailures() {
            return !failureReasons.isEmpty();
        }

        public boolean hasValidLines() {
            return !validLines.isEmpty();
        }

        public String firstFailureReason() {
            if (failureReasons.isEmpty()) {
                return "";
            }
            return failureReasons.get(0);
        }
    }
}
