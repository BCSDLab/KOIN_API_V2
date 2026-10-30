package in.koreatech.koin.infrastructure.s3.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import in.koreatech.koin.infrastructure.s3.dto.UploadFileResponse;
import in.koreatech.koin.infrastructure.s3.dto.UploadUrlResponse;
import in.koreatech.koin.global.exception.custom.KoinIllegalStateException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Error;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
public class S3ClientWrapper {

    private static final int URL_EXPIRATION_MINUTE = 10;

    private final String bucketName;
    private final String domainUrlPrefix;
    private final URI customDomainUri;
    private final S3Presigner.Builder presignerBuilder;
    private final S3Client awsS3Client;
    private final Clock clock;
    private final WebClient webClient;

    public S3ClientWrapper(
        @Value("${s3.bucket}") String bucketName,
        @Value("${s3.custom_domain}") String domainUrlPrefix,
        S3Presigner.Builder presignerBuilder,
        S3Client awsS3Client,
        Clock clock
    ) {
        this.bucketName = bucketName;
        this.domainUrlPrefix = domainUrlPrefix;
        this.customDomainUri = parseCustomDomainUri(domainUrlPrefix);
        this.presignerBuilder = presignerBuilder;
        this.awsS3Client = awsS3Client;
        this.clock = clock;
        this.webClient = WebClient.builder().build();
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

    public String getContentFromUrl(String url) {
        return fetchContent(url);
    }

    public boolean isCustomDomainUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return false;
        }
        String trimmed = url.trim();
        if (trimmed.chars().anyMatch(Character::isWhitespace)) {
            return false;
        }
        try {
            URI uri = URI.create(trimmed);
            return isAllowedCustomDomainUri(uri);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public String getContentFromCustomDomainUrl(String url) {
        String trimmed = url == null ? null : url.trim();
        if (!isCustomDomainUrl(trimmed)) {
            throw new KoinIllegalStateException("허용되지 않은 S3 URL입니다.");
        }
        return fetchContent(trimmed);
    }

    private String fetchContent(String url) {
        try {
            return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(5))
                .block();
        } catch (Exception e) {
            throw new KoinIllegalStateException("URL로 부터 데이터를 불러오던 중 문제가 발생했습니다. " + e.getMessage());
        }
    }

    private boolean isAllowedCustomDomainUri(URI uri) {
        return "https".equalsIgnoreCase(uri.getScheme())
            && uri.getUserInfo() == null
            && StringUtils.hasText(uri.getHost())
            && customDomainUri.getHost().equalsIgnoreCase(uri.getHost())
            && effectiveHttpsPort(customDomainUri) == effectiveHttpsPort(uri)
            && isPathWithinCustomDomain(uri);
    }

    private boolean isPathWithinCustomDomain(URI uri) {
        String allowedPath = normalizePath(customDomainUri.getPath());
        String candidatePath = normalizePath(uri.getPath());
        if (allowedPath == null || candidatePath == null) {
            return false;
        }
        if ("/".equals(allowedPath)) {
            return candidatePath.startsWith("/");
        }
        if (allowedPath.endsWith("/")) {
            return candidatePath.startsWith(allowedPath);
        }
        return candidatePath.equals(allowedPath) || candidatePath.startsWith(allowedPath + "/");
    }

    private static URI parseCustomDomainUri(String domainUrlPrefix) {
        try {
            URI uri = URI.create(domainUrlPrefix).normalize();
            if (!"https".equalsIgnoreCase(uri.getScheme())
                || !StringUtils.hasText(uri.getHost())
                || uri.getUserInfo() != null
                || uri.getQuery() != null
                || uri.getFragment() != null) {
                throw new IllegalArgumentException();
            }
            return uri;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("s3.custom_domain은 유효한 HTTPS URL이어야 합니다.", e);
        }
    }

    private static int effectiveHttpsPort(URI uri) {
        return uri.getPort() == -1 ? 443 : uri.getPort();
    }

    private static String normalizePath(String path) {
        String value = StringUtils.hasText(path) ? path : "/";
        if (value.indexOf('\\') >= 0) {
            return null;
        }
        try {
            return new URI(null, null, value, null).normalize().getPath();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public UploadFileResponse uploadFile(String uploadFilePath, byte[] fileData) {
        PutObjectRequest request = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(uploadFilePath)
            .acl(ObjectCannedACL.PUBLIC_READ)
            .build();
        awsS3Client.putObject(request, RequestBody.fromBytes(fileData));
        return new UploadFileResponse(domainUrlPrefix + uploadFilePath);
    }

    public void deleteFile(String s3Key) {
        try {
            awsS3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build());
        } catch (Exception e) {
            throw new KoinIllegalStateException("S3 객체 삭제 중 문제가 발생했습니다. " + e.getMessage());
        }
    }

    public void deleteFiles(List<String> s3Keys) {
        DeleteObjectsResponse response;
        try {
            List<ObjectIdentifier> keys = s3Keys.stream()
                .map(key -> ObjectIdentifier.builder().key(key).build())
                .toList();
            DeleteObjectsRequest request = DeleteObjectsRequest.builder()
                .bucket(bucketName)
                .delete(Delete.builder().objects(keys).build())
                .build();
            response = awsS3Client.deleteObjects(request);
        } catch (Exception e) {
            throw new KoinIllegalStateException("S3 객체 일괄 삭제 중 문제가 발생했습니다. : " + e.getMessage());
        }
        // v2는 일부 객체 삭제 실패를 예외 대신 응답의 errors로 돌려준다.
        if (response.hasErrors() && !response.errors().isEmpty()) {
            List<String> failedKeys = response.errors().stream()
                .map(S3Error::key)
                .toList();
            throw new KoinIllegalStateException("일부 S3 객체의 삭제가 실패했습니다. : " + failedKeys);
        }
    }

    public void downloadS3Object(String bucketName, String s3Key, File localFile) {
        GetObjectRequest request = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(s3Key)
            .build();
        try (InputStream inputStream = awsS3Client.getObject(request);
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

    public boolean doesFileExist(String key) {
        try {
            awsS3Client.headObject(HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
            return true;
        } catch (S3Exception e) {
            // v1 doesObjectExist와 같이 404만 "없음"으로 보고, 그 외 오류는 그대로 던진다.
            if (e.statusCode() == 404) {
                return false;
            }
            throw e;
        }
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
