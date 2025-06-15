package in.koreatech.koin.domain.user.verification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "user.verification")
public record VerificationProperties(
    Integer maxVerificationCount
) {

    public VerificationProperties {
        if (maxVerificationCount == null) {
            maxVerificationCount = 5;
        }
    }
}
