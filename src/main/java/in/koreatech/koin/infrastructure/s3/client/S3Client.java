package in.koreatech.koin.infrastructure.s3.client;

import static com.amazonaws.services.s3.model.DeleteObjectsRequest.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.MultiObjectDeleteException;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import in.koreatech.koin.infrastructure.s3.dto.UploadFileResponse;
import in.koreatech.koin.infrastructure.s3.dto.UploadUrlResponse;
import in.koreatech.koin._common.exception.custom.KoinIllegalStateException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
public class S3Client {

    private static final int URL_EXPIRATION_MINUTE = 10;

    private final String bucketName;
    private final String domainUrlPrefix;
    private final S3Presigner.Builder presignerBuilder;
    private final AmazonS3 amazonS3;
    private final Clock clock;

    public S3Client(
        @Value("${s3.bucket}") String bucketName,
        @Value("${s3.custom_domain}") String domainUrlPrefix,
        S3Presigner.Builder presignerBuilder,
        AmazonS3 amazonS3,
        Clock clock
    ) {
        this.bucketName = bucketName;
        this.domainUrlPrefix = domainUrlPrefix;
        this.presignerBuilder = presignerBuilder;
        this.amazonS3 = amazonS3;
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
        amazonS3.putObject(
            new PutObjectRequest(bucketName, uploadFilePath, new ByteArrayInputStream(fileData), metaData)
                .withCannedAcl(CannedAccessControlList.PublicRead));
        return new UploadFileResponse(domainUrlPrefix + uploadFilePath);
    }

    public void deleteFile(String s3Key) {
        try {
            amazonS3.deleteObject(bucketName, s3Key);
        } catch (Exception e) {
            throw new KoinIllegalStateException("S3 객체 삭제 중 문제가 발생했습니다. " + e.getMessage());
        }
    }

    public void deleteFiles(List<String> s3Keys) {
        try {
            List<KeyVersion> keys = s3Keys.stream()
                .map(KeyVersion::new)
                .toList();
            DeleteObjectsRequest request = new DeleteObjectsRequest(bucketName).withKeys(keys);
            amazonS3.deleteObjects(request);
        } catch (MultiObjectDeleteException e) {
            List<String> failedKeys = e.getErrors().stream()
                .map(MultiObjectDeleteException.DeleteError::getKey)
                .toList();
            throw new KoinIllegalStateException("일부 S3 객체의 삭제가 실패했습니다. : " + failedKeys);
        } catch (Exception e) {
            throw new KoinIllegalStateException("S3 객체 일괄 삭제 중 문제가 발생했습니다. : " + e.getMessage());
        }
    }

    public void downloadS3Object(String bucketName, String s3Key, File localFile) {
        try (S3Object s3Object = amazonS3.getObject(bucketName, s3Key);
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

    public String extractKeyFromUrl(String url) {
        if (!url.startsWith(domainUrlPrefix)) {
            throw new KoinIllegalStateException("잘못된 S3 URL입니다: " + url);
        }
        return url.substring(domainUrlPrefix.length());
    }

    public List<String> extractKeysFromUrls(List<String> urls) {
        return urls.stream()
            .map(this::extractKeyFromUrl)
            .toList();
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getDomainUrlPrefix() {
        return domainUrlPrefix;
    }
}
