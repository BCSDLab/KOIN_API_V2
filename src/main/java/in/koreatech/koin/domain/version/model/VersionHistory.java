package in.koreatech.koin.domain.version.model;

import static jakarta.persistence.GenerationType.IDENTITY;

import java.time.Clock;
import java.time.LocalDate;

import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

public class VersionHistory extends BaseEntity {


    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "version", nullable = false, length = 50)
    private String version;

    @NotNull
    @Column(name = "type", length = 50, unique = true)
    private String type;

    @Column(name = "message", length = 255)
    private String message;

    @Builder
    private VersionHistory(@NotNull String version, @NotNull String type, String message) {
        this.version = version;
        this.type = type;
        this.message = message;
    }

    public void update(Clock clock) {
        version = generateVersionName(clock);
    }

    private String generateVersionName(Clock clock) {
        String year = Integer.toString(LocalDate.now().getYear());
        String padding = "0_";
        String epochSeconds = Long.toString(clock.instant().getEpochSecond());
        return year + padding + epochSeconds;
    }
}
