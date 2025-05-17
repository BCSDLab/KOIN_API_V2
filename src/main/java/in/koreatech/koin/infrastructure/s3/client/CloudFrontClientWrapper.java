package in.koreatech.koin.infrastructure.s3.client;

import java.util.List;

import in.koreatech.koin._common.exception.custom.KoinIllegalStateException;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.CreateInvalidationRequest;
import software.amazon.awssdk.services.cloudfront.model.InvalidationBatch;
import software.amazon.awssdk.services.cloudfront.model.Paths;

@Slf4j
public class CloudFrontClientWrapper {

    private final CloudFrontClient cloudFrontClient;
    private final String distributionId;

    public CloudFrontClientWrapper(CloudFrontClient cloudFrontClient, String distributionId) {
        this.cloudFrontClient = cloudFrontClient;
        this.distributionId = distributionId;
    }

    public void invalidate(String path) {
        invalidateAll(List.of(path));
    }

    public void invalidateAll(List<String> paths) {
        try {
            List<String> normalizedPaths = paths.stream()
                .map(path -> path.startsWith("/") ? path : "/" + path)
                .distinct()
                .toList();
            if (normalizedPaths.isEmpty()) return;

            log.info("CloudFront Invalidate 요청 경로: {}", normalizedPaths);

            Paths cloudFrontPaths = Paths.builder()
                .items(normalizedPaths)
                .quantity(normalizedPaths.size())
                .build();

            InvalidationBatch batch = InvalidationBatch.builder()
                .paths(cloudFrontPaths)
                .callerReference(String.valueOf(System.currentTimeMillis()))
                .build();

            CreateInvalidationRequest request = CreateInvalidationRequest.builder()
                .distributionId(distributionId)
                .invalidationBatch(batch)
                .build();

            cloudFrontClient.createInvalidation(request);
        } catch (Exception e) {
            log.error("CloudFront invalidate 실패. 경로: {}", paths, e);
            throw new KoinIllegalStateException("CloudFront 캐시 무효화 중 문제가 발생했습니다. " + e.getMessage());
        }
    }
}
