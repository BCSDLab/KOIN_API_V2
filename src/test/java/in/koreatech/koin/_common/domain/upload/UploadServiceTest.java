package in.koreatech.koin._common.domain.upload;

import static in.koreatech.koin.integration.s3.model.ImageUploadDomain.OWNERS;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.integration.s3.dto.UploadUrlRequest;
import in.koreatech.koin.integration.s3.service.UploadService;
import in.koreatech.koin.integration.s3.client.S3Client;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.S3Presigner.Builder;

@Disabled
class UploadServiceTest extends AcceptanceTest {

    private AmazonS3 amazonS3;
    private Builder presigner;

    @Container
    public static LocalStackContainer localStackContainer = new LocalStackContainer(
        DockerImageName.parse("localstack/localstack"))
        .withServices(Service.S3);

    @BeforeEach
    void setUp() {
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(
            localStackContainer.getAccessKey(),
            localStackContainer.getSecretKey()
        );
        amazonS3 = AmazonS3ClientBuilder.standard()
            .withRegion(localStackContainer.getRegion())
            .withCredentials(new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(
                    localStackContainer.getAccessKey(),
                    localStackContainer.getSecretKey()
                ))
            )
            .build();

        presigner = S3Presigner.builder()
            .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
            .region(Region.of(localStackContainer.getRegion()));
    }

    @Test
    void 이미지_확장자를_받아_이미지_이름을_UUID로_생성_후_Presigned_URL을_생성하여_반환한다() {
        // given
        S3Client utils = new S3Client(
            "test-bucket",
            "https://test-image.koreatech.in/",
            presigner,
            amazonS3,
            Clock.systemDefaultZone()
        );

        // when
        var request = new UploadUrlRequest(
            1000,
            "image/png",
            "hello.png"
        );

        UploadService uploadService = new UploadService(utils, Clock.systemDefaultZone());
        var url = uploadService.getPresignedUrl(OWNERS, request);

        // then
        assertThat(url.preSignedUrl()).contains(
            "https://test-bucket.s3.amazonaws.com/",
            "OWNERS/",
            "hello.png"
        );
    }
}
