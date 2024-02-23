package in.koreatech.koin.global.domain.email.config;

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

    @Value("${aws.ses.access-key}")
    private String AWS_ACCESS_KEY_ID;

    @Value("${aws.ses.secret-key}")
    private String AWS_SECRET_KEY;

    @Bean
    public AmazonSimpleEmailServiceAsync amazonSimpleEmailServiceAsync() {
        BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(AWS_ACCESS_KEY_ID, AWS_SECRET_KEY);

        return AmazonSimpleEmailServiceAsyncClient.asyncBuilder()
            .withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials))
            .withRegion(Regions.AP_NORTHEAST_2)
            .build();
    }
}
