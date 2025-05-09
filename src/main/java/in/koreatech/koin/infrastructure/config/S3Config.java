package in.koreatech.koin.infrastructure.config;

import static software.amazon.awssdk.regions.Region.AP_NORTHEAST_2;

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

/**
 * Key 기반 인증 방식은 취약하므로 IAM Role 기반 인증 방식을 사용한다. (EC2ContainerCredentialsProviderWrapper)
 * EC2 인스턴스에 부여된 "EC2_to_S3" IAM Role을 통해 인증을 수행한다.
 */
@Configuration
public class S3Config {

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
            .withCredentials(new EC2ContainerCredentialsProviderWrapper()) // IAM Role 사용
            .build();
    }
}
