package in.koreatech.koin.global.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import static software.amazon.awssdk.regions.Region.AP_NORTHEAST_2;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

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
}
