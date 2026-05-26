package in.koreatech.koin.unit.domain.community.article.service.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryProperties;
import in.koreatech.koin.domain.community.article.service.summary.ArticleAttachmentSeed;
import in.koreatech.koin.domain.community.article.service.summary.ArticleDocumentParseClient;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryExternalApiException;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummarySource;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummarySourceReader;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummarySourceSeed;
import in.koreatech.koin.domain.community.article.service.summary.DocumentParseRequest;
import in.koreatech.koin.infrastructure.s3.client.S3Client;

class ArticleSummarySourceReaderTest {

    @Test
    void 본문_링크와_첨부파일을_파싱_대상으로_포함한다() {
        FakeDocumentParseClient parseClient = new FakeDocumentParseClient();
        ArticleAiSummaryProperties properties = new ArticleAiSummaryProperties();
        properties.setMaxDocumentsPerArticle(3);
        S3Client s3Client = mock(S3Client.class);
        when(s3Client.getDomainUrlPrefix()).thenReturn("https://static.koreatech.in/");
        ArticleSummarySourceReader reader = new ArticleSummarySourceReader(parseClient, properties, s3Client);
        ArticleSummarySourceSeed seed = new ArticleSummarySourceSeed(
            1,
            "장학금 안내",
            """
                <p>신청 안내입니다.</p>
                <a href="https://static.koreatech.in/files/scholarship.pdf">장학금 안내문</a>
                """,
            "학생처",
            LocalDate.of(2026, 5, 1),
            LocalDateTime.of(2026, 5, 1, 10, 0),
            List.of(new ArticleAttachmentSeed(
                10,
                "신청서.docx",
                "https://static.koreatech.in/files/application.docx",
                "hash",
                LocalDateTime.of(2026, 5, 1, 10, 0)
            ))
        );

        ArticleSummarySource source = reader.read(seed);

        assertThat(parseClient.requests)
            .extracting(DocumentParseRequest::url)
            .containsExactly(
                "https://static.koreatech.in/files/application.docx",
                "https://static.koreatech.in/files/scholarship.pdf"
            );
        assertThat(source.attachmentTexts()).hasSize(2);
        assertThat(source.attachmentTexts().get(0)).contains("파일명: 신청서.docx");
        assertThat(source.attachmentTexts().get(1)).contains("파일명: scholarship.pdf");
        assertThat(source.attachmentTexts().get(1)).contains("제출 서류: 신청서");
    }

    @Test
    void 본문에_텍스트로_노출된_이미지_URL도_파싱_대상으로_포함한다() {
        FakeDocumentParseClient parseClient = new FakeDocumentParseClient();
        ArticleAiSummaryProperties properties = new ArticleAiSummaryProperties();
        properties.setMaxDocumentsPerArticle(3);
        S3Client s3Client = mock(S3Client.class);
        when(s3Client.getDomainUrlPrefix()).thenReturn("https://static.koreatech.in/");
        ArticleSummarySourceReader reader = new ArticleSummarySourceReader(parseClient, properties, s3Client);
        ArticleSummarySourceSeed seed = new ArticleSummarySourceSeed(
            1,
            "포스터 안내",
            """
                <p>자세한 내용은 포스터를 확인하세요.</p>
                <p>https://static.koreatech.in/images/poster.png.</p>
                """,
            "학생처",
            LocalDate.of(2026, 5, 1),
            LocalDateTime.of(2026, 5, 1, 10, 0),
            List.of()
        );

        ArticleSummarySource source = reader.read(seed);

        assertThat(parseClient.requests)
            .extracting(DocumentParseRequest::url)
            .containsExactly("https://static.koreatech.in/images/poster.png");
        assertThat(source.attachmentTexts()).hasSize(1);
        assertThat(source.attachmentTexts().get(0)).contains("파일명: poster.png");
    }

    @Test
    void koreatech_ac_kr_하위_도메인_첨부_URL도_파싱_대상으로_포함한다() {
        FakeDocumentParseClient parseClient = new FakeDocumentParseClient();
        ArticleAiSummaryProperties properties = new ArticleAiSummaryProperties();
        properties.setMaxDocumentsPerArticle(3);
        S3Client s3Client = mock(S3Client.class);
        when(s3Client.getDomainUrlPrefix()).thenReturn("https://static.koreatech.in/");
        ArticleSummarySourceReader reader = new ArticleSummarySourceReader(parseClient, properties, s3Client);
        ArticleSummarySourceSeed seed = new ArticleSummarySourceSeed(
            1,
            "학사 안내",
            "<p>첨부 문서를 확인하세요.</p>",
            "학사팀",
            LocalDate.of(2026, 5, 1),
            LocalDateTime.of(2026, 5, 1, 10, 0),
            List.of(new ArticleAttachmentSeed(
                10,
                "notice.pdf",
                "https://portal.koreatech.ac.kr/files/notice.pdf",
                "hash",
                LocalDateTime.of(2026, 5, 1, 10, 0)
            ))
        );

        ArticleSummarySource source = reader.read(seed);

        assertThat(parseClient.requests)
            .extracting(DocumentParseRequest::url)
            .containsExactly("https://portal.koreatech.ac.kr/files/notice.pdf");
        assertThat(source.attachmentTexts()).hasSize(1);
        assertThat(source.attachmentTexts().get(0)).contains("파일명: notice.pdf");
    }

    @Test
    void 첨부_파싱에_실패해도_본문만으로_요약_입력을_구성한다() {
        ArticleDocumentParseClient parseClient = request -> {
            throw new IllegalStateException("parse failed");
        };
        ArticleAiSummaryProperties properties = new ArticleAiSummaryProperties();
        properties.setMaxDocumentsPerArticle(3);
        S3Client s3Client = mock(S3Client.class);
        when(s3Client.getDomainUrlPrefix()).thenReturn("https://static.koreatech.in/");
        ArticleSummarySourceReader reader = new ArticleSummarySourceReader(parseClient, properties, s3Client);
        ArticleSummarySourceSeed seed = new ArticleSummarySourceSeed(
            1,
            "장학금 안내",
            "<p>신청 기간은 5월 20일까지입니다.</p>",
            "학생처",
            LocalDate.of(2026, 5, 1),
            LocalDateTime.of(2026, 5, 1, 10, 0),
            List.of(new ArticleAttachmentSeed(
                10,
                "신청서.docx",
                "https://static.koreatech.in/files/application.docx",
                "hash",
                LocalDateTime.of(2026, 5, 1, 10, 0)
            ))
        );

        ArticleSummarySource source = reader.read(seed);

        assertThat(source.contentText()).isEqualTo("신청 기간은 5월 20일까지입니다.");
        assertThat(source.attachmentTexts()).isEmpty();
        assertThat(source.hasTemporaryAttachmentFailure()).isFalse();
        assertThat(source.mergedText()).isEqualTo("신청 기간은 5월 20일까지입니다.");
    }

    @Test
    void 첨부_파싱이_일시_실패하면_본문은_유지하고_일시_실패_여부를_표시한다() {
        ArticleDocumentParseClient parseClient = request -> {
            throw new ArticleSummaryExternalApiException("rate limited", true, null);
        };
        ArticleAiSummaryProperties properties = new ArticleAiSummaryProperties();
        properties.setMaxDocumentsPerArticle(3);
        S3Client s3Client = mock(S3Client.class);
        when(s3Client.getDomainUrlPrefix()).thenReturn("https://static.koreatech.in/");
        ArticleSummarySourceReader reader = new ArticleSummarySourceReader(parseClient, properties, s3Client);
        ArticleSummarySourceSeed seed = new ArticleSummarySourceSeed(
            1,
            "장학금 안내",
            "<p>신청 기간은 5월 20일까지입니다.</p>",
            "학생처",
            LocalDate.of(2026, 5, 1),
            LocalDateTime.of(2026, 5, 1, 10, 0),
            List.of(new ArticleAttachmentSeed(
                10,
                "신청서.docx",
                "https://static.koreatech.in/files/application.docx",
                "hash",
                LocalDateTime.of(2026, 5, 1, 10, 0)
            ))
        );

        ArticleSummarySource source = reader.read(seed);

        assertThat(source.contentText()).isEqualTo("신청 기간은 5월 20일까지입니다.");
        assertThat(source.attachmentTexts()).isEmpty();
        assertThat(source.hasTemporaryAttachmentFailure()).isTrue();
        assertThat(source.mergedText()).isEqualTo("신청 기간은 5월 20일까지입니다.");
    }

    @Test
    void 허용된_koreatech_하위_도메인_본문_URL이면_URL_내용을_조회해_요약_입력으로_사용한다() {
        FakeDocumentParseClient parseClient = new FakeDocumentParseClient();
        ArticleAiSummaryProperties properties = new ArticleAiSummaryProperties();
        S3Client s3Client = mock(S3Client.class);
        when(s3Client.getDomainUrlPrefix()).thenReturn("https://static.koreatech.in/");
        when(s3Client.getContentFromUrl("https://stage-static.koreatech.in/articles/content/notice.txt"))
            .thenReturn("<p>신청 기간은 5월 20일까지입니다.</p>");
        ArticleSummarySourceReader reader = new ArticleSummarySourceReader(parseClient, properties, s3Client);
        ArticleSummarySourceSeed seed = new ArticleSummarySourceSeed(
            1,
            "장학금 안내",
            "https://stage-static.koreatech.in/articles/content/notice.txt",
            "학생처",
            LocalDate.of(2026, 5, 1),
            LocalDateTime.of(2026, 5, 1, 10, 0),
            List.of()
        );

        ArticleSummarySource source = reader.read(seed);

        assertThat(source.contentText()).isEqualTo("신청 기간은 5월 20일까지입니다.");
        assertThat(source.mergedText()).isEqualTo("신청 기간은 5월 20일까지입니다.");
    }

    @Test
    void 허용되지_않은_본문_URL은_요약_입력에서_제외한다() {
        FakeDocumentParseClient parseClient = new FakeDocumentParseClient();
        ArticleAiSummaryProperties properties = new ArticleAiSummaryProperties();
        S3Client s3Client = mock(S3Client.class);
        when(s3Client.getDomainUrlPrefix()).thenReturn("https://static.koreatech.in/");
        ArticleSummarySourceReader reader = new ArticleSummarySourceReader(parseClient, properties, s3Client);
        ArticleSummarySourceSeed seed = new ArticleSummarySourceSeed(
            1,
            "장학금 안내",
            "https://stage-static.koreatech.in.evil.com/articles/content/notice.txt",
            "학생처",
            LocalDate.of(2026, 5, 1),
            LocalDateTime.of(2026, 5, 1, 10, 0),
            List.of()
        );

        ArticleSummarySource source = reader.read(seed);

        assertThat(source.contentText()).isEmpty();
        assertThat(source.mergedText()).isEmpty();
        verify(s3Client, never()).getContentFromUrl("https://stage-static.koreatech.in.evil.com/articles/content/notice.txt");
    }

    private static class FakeDocumentParseClient implements ArticleDocumentParseClient {

        private final List<DocumentParseRequest> requests = new ArrayList<>();

        @Override
        public String parse(DocumentParseRequest request) {
            requests.add(request);
            return "대상: 재학생\n제출 서류: 신청서";
        }
    }
}
