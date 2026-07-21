package in.koreatech.koin.unit.infrastructure.s3.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.time.Clock;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.amazonaws.services.s3.AmazonS3;

import in.koreatech.koin.global.exception.custom.KoinIllegalStateException;
import in.koreatech.koin.infrastructure.s3.client.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

class S3ClientTest {

    private S3Client s3Client;

    @BeforeEach
    void setUp() {
        s3Client = new S3Client(
            "test-bucket",
            "https://static.koreatech.in/articles/",
            mock(S3Presigner.Builder.class),
            mock(AmazonS3.class),
            Clock.systemUTC()
        );
    }

    @Test
    void 설정된_도메인과_경로에_속한_HTTPS_URL만_허용한다() {
        assertThat(s3Client.isCustomDomainUrl(
            "  https://static.koreatech.in/articles/content/notice.html?version=1#summary  "
        )).isTrue();
        assertThat(s3Client.isCustomDomainUrl(
            "https://static.koreatech.in:443/articles/content/notice.html"
        )).isTrue();
    }

    @Test
    void 다른_호스트나_포트로_향하는_URL은_허용하지_않는다() {
        List<String> disallowedUrls = List.of(
            "https://example.com/articles/content/notice.html",
            "https://static.koreatech.in.evil.com/articles/content/notice.html",
            "https://static.koreatech.in@evil.com/articles/content/notice.html",
            "https://evil.com@static.koreatech.in/articles/content/notice.html",
            "https://static.koreatech.in:8443/articles/content/notice.html",
            "http://static.koreatech.in/articles/content/notice.html"
        );

        assertThat(disallowedUrls).allSatisfy(url -> assertThat(s3Client.isCustomDomainUrl(url)).isFalse());
    }

    @Test
    void 설정된_경로를_벗어나는_URL은_허용하지_않는다() {
        List<String> disallowedUrls = List.of(
            "https://static.koreatech.in/article/content/notice.html",
            "https://static.koreatech.in/articles-malicious/content/notice.html",
            "https://static.koreatech.in/articles/../private/notice.html",
            "https://static.koreatech.in/articles/%2e%2e/private/notice.html"
        );

        assertThat(disallowedUrls).allSatisfy(url -> assertThat(s3Client.isCustomDomainUrl(url)).isFalse());
    }

    @Test
    void 허용되지_않은_URL은_HTTP_요청_전에_차단한다() {
        assertThatThrownBy(() -> s3Client.getContentFromCustomDomainUrl(
            "https://static.koreatech.in.evil.com/articles/content/notice.html"
        ))
            .isInstanceOf(KoinIllegalStateException.class)
            .hasMessage("허용되지 않은 S3 URL입니다.");
    }
}
