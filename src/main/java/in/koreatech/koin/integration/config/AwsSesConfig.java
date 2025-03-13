package in.koreatech.koin.integration.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsync;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsyncClient;

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
    public AmazonSimpleEmailServiceAsync amazonSimpleEmailServiceAsync() {
        BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(accessKey, secretKey);

        return AmazonSimpleEmailServiceAsyncClient.asyncBuilder()
            .withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials))
            .withRegion(Regions.US_WEST_2)
            .build();
    }
}
