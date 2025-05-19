package in.koreatech.koin._common.datacleaner;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cleanup")
public record CleanupProperties(
    List<String> targetTables
) {

}
