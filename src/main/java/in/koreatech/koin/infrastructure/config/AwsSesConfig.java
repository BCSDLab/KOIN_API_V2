package in.koreatech.koin.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
public class AwsSesConfig {

    private final String accessKey;
    private final String secretKey;

    public AwsSesConfig(
        @Value("${aws.ses.access-key}") String accessKey,
        @Value("${aws.ses.secret-key}") String secretKey
    ) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    @Bean
    public SesClient sesClient() {
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        return SesClient.builder()
            .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
            .region(Region.US_WEST_2)
            .build();
    }
}
