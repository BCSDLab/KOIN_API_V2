package in.koreatech.koin.unit.domain.community.article.service.summary;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryProperties;

class ArticleAiSummaryPropertiesTest {

    @Test
    void 재선별_재시도는_최대_한_번으로_제한한다() {
        ArticleAiSummaryProperties properties = new ArticleAiSummaryProperties();
        properties.setMaxRefinementRetryCount(10);

        assertThat(properties.getBoundedMaxRefinementRetryCount()).isEqualTo(1);
    }
}
