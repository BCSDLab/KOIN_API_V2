package in.koreatech.koin.global.s3;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import in.koreatech.koin.global.domain.upload.dto.UploadFileResponse;
import in.koreatech.koin.global.domain.upload.dto.UploadUrlResponse;
import in.koreatech.koin.global.exception.KoinIllegalStateException;
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

    public void downloadS3Object(String bucketName, String s3Key, File localFile) {
        try (S3Object s3Object = s3Client.getObject(bucketName, s3Key);
             InputStream inputStream = s3Object.getObjectContent();
             OutputStream outputStream = new FileOutputStream(localFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new KoinIllegalStateException("S3 객체 다운로드 중 문제가 발생했습니다. " + e.getMessage());
        }
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getDomainUrlPrefix() {
        return domainUrlPrefix;
    }
}
