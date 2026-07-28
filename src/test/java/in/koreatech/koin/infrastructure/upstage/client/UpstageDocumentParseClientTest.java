package in.koreatech.koin.infrastructure.upstage.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

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

    @Test
    void 기존_8천자를_넘는_문서_추출문도_보존한다() {
        String parsedText = "가".repeat(20_000) + "신청 마감은 6월 30일입니다.";

        String result = ReflectionTestUtils.invokeMethod(client, "truncate", parsedText);

        assertThat(result).isEqualTo(parsedText);
    }

    @Test
    void 문서_추출문이_상한을_넘으면_앞부분과_뒷부분을_함께_보존한다() {
        String finalSchedule = "최종 신청 일정은 7월 31일까지입니다.";
        String parsedText = "가".repeat(100_000) + finalSchedule;

        String result = ReflectionTestUtils.invokeMethod(client, "truncate", parsedText);

        assertThat(result)
            .hasSize(96_000)
            .contains("중간 내용은 길이 제한으로 생략됨")
            .endsWith(finalSchedule);
    }
}
