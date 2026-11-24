package in.koreatech.koin.infrastructure.upstage.client;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.koreatech.koin.domain.translation.model.TranslationReason;
import in.koreatech.koin.domain.translation.service.TranslationFailureException;

/** Solar가 금액·시간·URL과 호칭이 붙은 비한글 이름을 바꾸지 않도록 원문 조각을 복원한다. */
record TranslationTextProtection(Map<String, String> fields, Map<String, Map<String, String>> tokens,
                                 String prefix, Map<String, String> originals, Map<String, String> suffixes) {
    private static final Pattern URL = Pattern.compile("https?://[^\\s<>\"']+");
    private static final Pattern TRAILING_URLS = Pattern.compile("(?:\\s+https?://[^\\s<>\"']+)+\\s*$");
    private static final Pattern VALUE = Pattern.compile(
        // 山田님·John님처럼 '님'이 붙은 비한글 이름만 보호한다. 손님·고객님 같은 일반 명사는 번역한다.
        "https?://[^\\s<>\"']+|[\\p{L}\\p{M}&&[^\\p{IsHangul}]]+(?=님)"
            + "|(?:[₩$€£¥]\\s*)?\\d[\\d,]*(?:\\.\\d+)?(?::\\d{2})?"
            + "(?:\\s*(?:KRW|USD|EUR|JPY|CNY|원|달러|유로|엔|위안|%))?");

    static TranslationTextProtection protect(Map<String, String> source) {
        String prefix = "__KOIN_";
        String original = String.join("\n", source.values());
        while (original.contains(prefix)) {
            prefix += "_";
        }
        Map<String, String> fields = new LinkedHashMap<>();
        Map<String, String> suffixes = new LinkedHashMap<>();
        Map<String, Map<String, String>> tokens = new LinkedHashMap<>();
        int sequence = 0;
        for (var field : source.entrySet()) {
            Map<String, String> replacements = new LinkedHashMap<>();
            String text = field.getValue();
            Matcher trailing = TRAILING_URLS.matcher(text);
            // 끝에 독립적으로 붙은 주소는 번역할 문장에 포함하지 않고 같은 위치에 복원한다.
            if (trailing.find() && !text.substring(0, trailing.start()).isBlank()) {
                suffixes.put(field.getKey(), text.substring(trailing.start()));
                text = text.substring(0, trailing.start());
            }
            Matcher matcher = VALUE.matcher(text);
            StringBuilder protectedText = new StringBuilder();
            while (matcher.find()) {
                if (URL.matcher(matcher.group()).matches()) {
                    matcher.appendReplacement(protectedText, Matcher.quoteReplacement(matcher.group()));
                    continue;
                }
                String token = prefix + sequence++ + "__";
                replacements.put(token, matcher.group());
                matcher.appendReplacement(protectedText, Matcher.quoteReplacement(token));
            }
            matcher.appendTail(protectedText);
            fields.put(field.getKey(), protectedText.toString());
            tokens.put(field.getKey(), replacements);
        }
        return new TranslationTextProtection(fields, tokens, prefix, source, suffixes);
    }

    Map<String, String> restore(Map<String, String> translated) {
        Map<String, String> restored = new LinkedHashMap<>();
        for (var field : translated.entrySet()) {
            String text = field.getValue();
            for (var token : tokens.get(field.getKey()).entrySet()) {
                int position = text.indexOf(token.getKey());
                if (position < 0 || text.indexOf(token.getKey(), position + token.getKey().length()) >= 0) {
                    throw invalid();
                }
                text = text.replace(token.getKey(), token.getValue());
            }
            if (text.contains(prefix)) {
                throw invalid();
            }
            text += suffixes.getOrDefault(field.getKey(), "");
            // 문장 내부 주소까지 전체 URL과 개수를 비교한다.
            if (!urlCounts(text).equals(urlCounts(originals.get(field.getKey())))) {
                throw invalid();
            }
            restored.put(field.getKey(), text);
        }
        return restored;
    }

    private Map<String, Integer> urlCounts(String text) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        Matcher matcher = URL.matcher(text);
        while (matcher.find()) {
            counts.merge(matcher.group(), 1, Integer::sum);
        }
        return counts;
    }

    private TranslationFailureException invalid() {
        return new TranslationFailureException(TranslationReason.INVALID_TRANSLATION, null);
    }
}
