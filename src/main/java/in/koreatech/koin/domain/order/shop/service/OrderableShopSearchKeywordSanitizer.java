package in.koreatech.koin.domain.order.shop.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

/**
 * 주문 가능 상점 검색 키워드 정제기
 * 1. 공백 제거
 * 2. 한글 자음, 모음을 제거 ex) 짜장ㅁ => 짜장
 * 3. 특수문자 제거
 */
@Component
public class OrderableShopSearchKeywordSanitizer {

    private static final String BLANK_REGEX = "\\s+";
    private static final String EMPTY = "";
    private static final String HANGUL_CONSONANTS = "[ㄱ-ㅎ]";
    private static final String HANGUL_VOWELS = "[ㅏ-ㅣ]";
    private static final String ALLOWED_CHARACTERS_REGEX = "[^가-힣a-zA-Z0-9]";

    public List<String> sanitizeToKeywords(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(keyword.trim().split(BLANK_REGEX))
            .map(this::processKeyword)
            .filter(k -> !k.isBlank())
            .collect(Collectors.toList());
    }

    /**
     * 키워드 정리: 특수문자 제거 → 불완전한 한글 제거
     */
    private String processKeyword(String input) {
        if (input == null) return null;

        return removeIncompleteHangul(removeSpecialCharacters(input));
    }

    /**
     * 불완전한 한글(자음, 모음) 제거
     */
    private String removeIncompleteHangul(String input) {
        if (input == null) return null;

        return input.replaceAll(HANGUL_CONSONANTS, EMPTY)
            .replaceAll(HANGUL_VOWELS, EMPTY);
    }

    /**
     * 특수문자 제거 - 허용된 문자(한글, 영문, 숫자)만 남김
     */
    private String removeSpecialCharacters(String input) {
        if (input == null) return null;

        return input.replaceAll(ALLOWED_CHARACTERS_REGEX, EMPTY);
    }
}
