package in.koreatech.koin.infrastructure.cloudfront.client;

import java.util.List;

import in.koreatech.koin._common.exception.custom.KoinIllegalStateException;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.CreateInvalidationRequest;
import software.amazon.awssdk.services.cloudfront.model.InvalidationBatch;
import software.amazon.awssdk.services.cloudfront.model.Paths;

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
            throw new KoinIllegalStateException("CloudFront 캐시 무효화 중 문제가 발생했습니다. " + e.getMessage());
        }
    }
}
