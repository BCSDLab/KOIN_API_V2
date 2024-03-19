package in.koreatech.koin.global.domain.upload;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.StringJoiner;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@Transactional(readOnly = true)
public class UploadService {

    private static final int URL_EXPIRATION_MINUTE = 10;

    private final S3Presigner.Builder presignerBuilder;
    private final String bucketName;
    private final String domainUrlPrefix;
    private final Clock clock;

    public UploadService(
        S3Presigner.Builder presigner,
        @Value("${s3.bucket}") String bucketName,
        @Value("${s3.custom_domain}") String domainUrlPrefix,
        Clock clock
    ) {
        this.presignerBuilder = presigner;
        this.bucketName = bucketName;
        this.domainUrlPrefix = domainUrlPrefix;
        this.clock = clock;
    }

    public UploadUrlResponse getPresignedUrl(ImageUploadDomain domain, UploadUrlRequest request) {
        try (S3Presigner presigner = presignerBuilder.build()) {
            String uploadFilePath = generateFilePath(domain.name(), request.fileName());
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

    private String generateFilePath(String domainName, String fileName) {
        var now = LocalDateTime.now(clock);
        StringJoiner uploadPrefix = new StringJoiner("/");
        uploadPrefix.add("upload")
            .add(domainName)
            .add(String.valueOf(now.getYear()))
            .add(String.valueOf(now.getMonth().getValue()))
            .add(String.valueOf(now.getDayOfMonth()))
            .add(UUID.randomUUID().toString());

        String[] split = fileName.split("\\.");
        String fileExt = split[split.length - 1];
        return uploadPrefix + "." + fileExt;
    }
}
