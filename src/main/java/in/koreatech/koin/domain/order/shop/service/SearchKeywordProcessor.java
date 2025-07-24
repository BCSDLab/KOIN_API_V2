package in.koreatech.koin.domain.order.shop.service;

import org.springframework.stereotype.Component;

@Component
public class SearchKeywordProcessor {

    private static final String BLANK_REGEX = "\\s+";
    private static final String EMPTY = "";
    private static final String HANGUL_CONSONANTS = "[ㄱ-ㅎ]";
    private static final String HANGUL_VOWELS = "[ㅏ-ㅣ]";

    public String process(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return EMPTY;
        }
        String removeBlank = keyword.replaceAll(BLANK_REGEX, EMPTY);
        return removeIncompleteHangul(removeBlank);
    }

    private String removeIncompleteHangul(String input) {
        if (input == null) return null;

        return input.replaceAll(HANGUL_CONSONANTS, "")
            .replaceAll(HANGUL_VOWELS, "");
    }
}
