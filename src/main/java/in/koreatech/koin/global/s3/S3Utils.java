package in.koreatech.koin.global.s3;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import in.koreatech.koin.global.domain.upload.dto.UploadUrlResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
public class S3Utils {

    private static final int URL_EXPIRATION_MINUTE = 10;

    private final S3Presigner.Builder presignerBuilder;
    private final Clock clock;
    private final String bucketName;
    private final String domainUrlPrefix;

    public S3Utils(
        S3Presigner.Builder presignerBuilder,
        Clock clock,
        @Value("${s3.bucket}") String bucketName,
        @Value("${s3.custom_domain}") String domainUrlPrefix
    ) {
        this.presignerBuilder = presignerBuilder;
        this.clock = clock;
        this.bucketName = bucketName;
        this.domainUrlPrefix = domainUrlPrefix;
    }

    public UploadUrlResponse getUploadUrl(String uploadFilePath) {
        try (S3Presigner presigner = presignerBuilder.build()) {
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(URL_EXPIRATION_MINUTE))
                .putObjectRequest(builder -> builder
                    .bucket(bucketName)
                    .key(uploadFilePath)
                    .build()
                ).build();

            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
            return new UploadUrlResponse(
                presignedRequest.url().toExternalForm(),
                domainUrlPrefix + uploadFilePath,
                LocalDateTime.now(clock).plusMinutes(URL_EXPIRATION_MINUTE)
            );
        }
    }
}
