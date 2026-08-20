package in.koreatech.koin.unit.domain.community.article.service.summary;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryProperties;

class ArticleAiSummaryPropertiesTest {

    @Test
    void Solar_Pro4_모델과_v11_프롬프트를_기본값으로_사용한다() {
        ArticleAiSummaryProperties properties = new ArticleAiSummaryProperties();

        assertThat(properties.getModel()).isEqualTo("solar-pro4");
        assertThat(properties.getPromptVersion()).isEqualTo("v11");
        assertThat(properties.getChatRequestTimeoutSeconds()).isEqualTo(600);
    }
}
