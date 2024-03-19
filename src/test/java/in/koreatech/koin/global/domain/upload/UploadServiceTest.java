package in.koreatech.koin.global.domain.upload;

import java.time.Clock;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.global.domain.upload.dto.UploadUrlRequest;
import static in.koreatech.koin.global.domain.upload.model.ImageUploadDomain.OWNERS;
import in.koreatech.koin.global.domain.upload.service.UploadService;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.S3Presigner.Builder;

class UploadServiceTest extends AcceptanceTest {

    @Container
    public LocalStackContainer localStackContainer = new LocalStackContainer(
        DockerImageName.parse("localstack/localstack"))
        .withServices(Service.S3);

    private final String bucketName;
    private final String domainUrlPrefix;

    UploadServiceTest(
        @Value(value = "${s3.bucket}")
        String bucketName,
        @Value(value = "${s3.custom_domain}")
        String domainUrlPrefix
    ) {
        this.bucketName = bucketName;
        this.domainUrlPrefix = domainUrlPrefix;
    }

    @Test
    void 이미지_확장자를_받아_이미지_이름을_UUID로_생성_후_Presigned_URL을_생성하여_반환한다() {
        when(clock.instant()).thenReturn(
            ZonedDateTime.parse("2024-02-21 18:00:00 KST", ofPattern("yyyy-MM-dd " + "HH:mm:ss z")).toInstant());
        when(clock.getZone()).thenReturn(Clock.systemDefaultZone().getZone());

        // given
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(
            localStackContainer.getAccessKey(),
            localStackContainer.getSecretKey()
        );
        Builder presignerBuilder = S3Presigner.builder()
            .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
            .region(Region.of(localStackContainer.getRegion()));

        var uploadService = new UploadService(
            presignerBuilder,
            bucketName,
            domainUrlPrefix,
            clock
        );

        // when
        var request = new UploadUrlRequest(
            1000,
            "image/png",
            "hello.png"
        );
        var url = uploadService.getPresignedUrl(OWNERS, request);

        // then
        assertThat(url.preSignedUrl()).contains(
            "https://test-bucket.s3.amazonaws.com/",
            "OWNERS/",
            ".png",
            "X-Amz-Expires=" + 60 * 10
        );
    }
}
