package in.koreatech.koin.global.datacleaner;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cleanup")
public record CleanupProperties(
    Integer retentionDays,
    List<String> targetTables
) {

}
