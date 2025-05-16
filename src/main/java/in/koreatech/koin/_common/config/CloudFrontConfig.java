package in.koreatech.koin._common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import in.koreatech.koin.infrastructure.s3.client.CloudFrontClientWrapper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;

@Configuration
@Profile("!test")
public class CloudFrontConfig {

    @Value("${cloudfront.distribution-id}")
    private String distributionId;

    @Value("${cloudfront.region}")
    private String region;

    @Bean
    public CloudFrontClient cloudFrontClient() {
        return CloudFrontClient.builder()
            .region(Region.of(region))
            .build();
    }

    @Bean
    public CloudFrontClientWrapper cloudFrontClientWrapper(CloudFrontClient cloudFrontClient) {
        return new CloudFrontClientWrapper(cloudFrontClient, distributionId);
    }
}
