package in.koreatech.koin.domain.translation.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "translation")
public class TranslationProperties {
    @NotBlank
    @Size(max = 100)
    private String model = "solar-pro4";
    @NotBlank
    @Size(max = 20)
    private String promptVersion = "v2";
    @Min(1) @Max(60)
    private int requestTimeoutSeconds = 25;
    @Min(1) @Max(20)
    private int maxConcurrentRequests = 4;
    @Min(1) @Max(1000)
    private int maxRequestsPerMinute = 30;
    @Min(1) @Max(20000)
    private int maxInputCharacters = 8000;
    @Min(1) @Max(3600)
    private int retryDelaySeconds = 30;
    @Min(1) @Max(30)
    private int contentRetentionDays = 30;
    @Min(1) @Max(7)
    private int messageRetentionDays = 1;
}
