package in.koreatech.koin.domain.dining.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "dining.report.delivery")
public class DiningReportDeliveryProperties {

    private String workspaceId;
    private String channelId;

    public boolean isConfigured() {
        return StringUtils.hasText(workspaceId) && StringUtils.hasText(channelId);
    }
}
