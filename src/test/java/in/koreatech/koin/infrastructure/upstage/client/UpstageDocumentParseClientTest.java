package in.koreatech.koin.infrastructure.upstage.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryProperties;

class UpstageDocumentParseClientTest {

    private final UpstageDocumentParseClient client = new UpstageDocumentParseClient(
        new ObjectMapper(),
        new UpstageProperties(),
        new ArticleAiSummaryProperties()
    );

    @Test
    void content_markdown이_있으면_elements를_중복으로_붙이지_않는다() throws Exception {
        String rawResponse = """
            {
              "content": {
                "markdown": "신청 기간은 5월 20일까지입니다."
              },
              "elements": [
                {
                  "content": {
                    "markdown": "신청 기간은 5월 20일까지입니다."
                  }
                }
              ]
            }
            """;

        String text = client.extractText(rawResponse);

        assertThat(text).isEqualTo("신청 기간은 5월 20일까지입니다.");
    }

    @Test
    void content가_없으면_elements의_markdown을_사용한다() throws Exception {
        String rawResponse = """
            {
              "elements": [
                {
                  "content": {
                    "markdown": "대상은 재학생입니다."
                  }
                },
                {
                  "content": {
                    "text": "신청서는 이메일로 제출합니다."
                  }
                }
              ]
            }
            """;

        String text = client.extractText(rawResponse);

        assertThat(text).isEqualTo("대상은 재학생입니다.\n신청서는 이메일로 제출합니다.");
    }
}
