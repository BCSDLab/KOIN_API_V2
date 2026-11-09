package in.koreatech.koin.infrastructure.upstage.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Map;

import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.translation.service.TranslationFailureException;

class TranslationTextProtectionTest {
    @Test
    void 통화와_시간_URL은_원문으로_복원하고_누락하거나_복제한_결과는_거절한다() {
        var protectedText = TranslationTextProtection.protect(Map.of(
            "content", "가격은 12000원, 만날 시간은 15:30.\nhttps://example.com/menu?id=17"));
        var tokens = new ArrayList<>(protectedText.tokens().get("content").keySet());
        String translated = "Price: " + tokens.get(0) + ", time: " + tokens.get(1) + ".";
        assertThat(protectedText.restore(Map.of("content", translated))).containsEntry(
            "content", "Price: 12000원, time: 15:30.\nhttps://example.com/menu?id=17");
        assertThatThrownBy(() -> protectedText.restore(Map.of("content", translated.replace(tokens.get(0), "12000 yen"))))
            .isInstanceOf(TranslationFailureException.class).hasMessage("INVALID_TRANSLATION");
        assertThatThrownBy(() -> protectedText.restore(Map.of("content", translated + tokens.get(0))))
            .isInstanceOf(TranslationFailureException.class).hasMessage("INVALID_TRANSLATION");
        var embedded = TranslationTextProtection.protect(Map.of("content", "안내 https://example.com/pay 에서 확인"));
        assertThatThrownBy(() -> embedded.restore(Map.of("content", "Visit https://example.com/pay-wrong for details")))
            .isInstanceOf(TranslationFailureException.class).hasMessage("INVALID_TRANSLATION");
        assertThatThrownBy(() -> embedded.restore(Map.of("content", "Visit https://example.com/pay https://example.com/extra")))
            .isInstanceOf(TranslationFailureException.class).hasMessage("INVALID_TRANSLATION");
        assertThatThrownBy(() -> protectedText.restore(Map.of("content", translated + " https://example.com/extra")))
            .isInstanceOf(TranslationFailureException.class).hasMessage("INVALID_TRANSLATION");
    }
}
