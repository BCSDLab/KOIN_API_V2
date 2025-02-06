package in.koreatech.koin.global.config;

import static software.amazon.awssdk.regions.Region.AP_NORTHEAST_2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.EC2ContainerCredentialsProviderWrapper;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    private final String accessKey;
    private final String secretKey;

    public S3Config(
        @Value("${s3.key}") String accessKey,
        @Value("${s3.secret}") String secretKey
    ) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    /**
     * S3Presigner 사용 후 close()를 권장하므로, Builder 를 반환하여 필요 시 객체를 만들어 사용 후 close 되도록 구현.
     */
    @Bean
    public S3Presigner.Builder s3PresignerBuilder() {
        return S3Presigner.builder()
            .credentialsProvider(InstanceProfileCredentialsProvider.create())
            .region(AP_NORTHEAST_2);
    }

    @Bean
    public AmazonS3 amazonS3ClientBuilder() {
        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setProtocol(Protocol.HTTPS);

        return AmazonS3ClientBuilder.standard()
            .withRegion(Regions.AP_NORTHEAST_2)
            .withClientConfiguration(clientConfig)
            //.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey))) // IAM user 사용
            .withCredentials(new EC2ContainerCredentialsProviderWrapper()) // IAM Role 사용
            .build();
    }
}
