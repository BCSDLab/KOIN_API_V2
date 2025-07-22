package in.koreatech.koin.domain.order.shop.service;

public class HangulCleaner {

    // 한글 자음만 있는 경우 (ㄱ-ㅎ)
    private static final String HANGUL_CONSONANTS = "[ㄱ-ㅎ]";

    // 한글 모음만 있는 경우 (ㅏ-ㅣ)
    private static final String HANGUL_VOWELS = "[ㅏ-ㅣ]";

    public static String removeIncompleteHangul(String input) {
        if (input == null) return null;

        return input.replaceAll(HANGUL_CONSONANTS, "")
            .replaceAll(HANGUL_VOWELS, "");
    }
}
