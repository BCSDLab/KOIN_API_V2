package in.koreatech.koin.unit.domain.community.article.service.summary;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryProperties;

class ArticleAiSummaryPropertiesTest {

    @Test
    void Solar_Pro4_모델과_v12_프롬프트를_기본값으로_사용한다() {
        ArticleAiSummaryProperties properties = new ArticleAiSummaryProperties();

        assertThat(properties.getModel()).isEqualTo("solar-pro4");
        assertThat(properties.getPromptVersion()).isEqualTo("v12");
        assertThat(properties.getChatRequestTimeoutSeconds()).isEqualTo(600);
        assertThat(properties.getProcessingLockDuration()).isEqualTo(Duration.ofMinutes(53).plusSeconds(3));
        properties.setMaxRefinementRetryCount(2);
        assertThat(properties.getProcessingLockDuration()).isEqualTo(Duration.ofMinutes(63).plusSeconds(3));
        properties.setLockMinutes(90);
        assertThat(properties.getProcessingLockDuration()).isEqualTo(Duration.ofMinutes(90));
    }
}
