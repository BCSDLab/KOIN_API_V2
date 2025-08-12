package in.koreatech.koin.domain.order.shop.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

/**
 * 주문 가능 상점 검색 키워드 전처리
 * 1. 공백 제거
 * 2. 한글 자음, 모음을 제거 ex) 짜장ㅁ => 짜장
 * */
@Component
public class SearchKeywordProcessor {

    private static final String BLANK_REGEX = "\\s+";
    private static final String EMPTY = "";
    private static final String HANGUL_CONSONANTS = "[ㄱ-ㅎ]";
    private static final String HANGUL_VOWELS = "[ㅏ-ㅣ]";

    public List<String> processToKeywords(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(keyword.trim().split(BLANK_REGEX))
            .map(this::removeIncompleteHangul)
            .filter(k -> !k.isBlank())
            .collect(Collectors.toList());
    }

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
