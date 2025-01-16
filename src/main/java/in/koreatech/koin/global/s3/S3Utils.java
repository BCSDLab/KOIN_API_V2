package in.koreatech.koin.global.s3;

import java.io.ByteArrayInputStream;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import in.koreatech.koin.global.domain.upload.dto.UploadFileResponse;
import in.koreatech.koin.global.domain.upload.dto.UploadUrlResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
public class S3Utils {

    private static final int URL_EXPIRATION_MINUTE = 10;

    private final String bucketName;
    private final String domainUrlPrefix;
    private final S3Presigner.Builder presignerBuilder;
    private final AmazonS3 s3Client;
    private final Clock clock;

    public S3Utils(
        @Value("${s3.bucket}") String bucketName,
        @Value("${s3.custom_domain}") String domainUrlPrefix,
        S3Presigner.Builder presignerBuilder,
        AmazonS3 s3Client,
        Clock clock
    ) {
        this.bucketName = bucketName;
        this.domainUrlPrefix = domainUrlPrefix;
        this.presignerBuilder = presignerBuilder;
        this.s3Client = s3Client;
        this.clock = clock;
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

    public UploadFileResponse uploadFile(String uploadFilePath, byte[] fileData) {
        ObjectMetadata metaData = new ObjectMetadata();
        metaData.setContentLength(fileData.length);
        s3Client.putObject(
            new PutObjectRequest(bucketName, uploadFilePath, new ByteArrayInputStream(fileData), metaData)
                .withCannedAcl(CannedAccessControlList.PublicRead));
        return new UploadFileResponse(domainUrlPrefix + uploadFilePath);
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getDomainUrlPrefix() {
        return domainUrlPrefix;
    }
}
