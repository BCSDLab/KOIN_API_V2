package in.koreatech.koin.domain.version.model;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import in.koreatech.koin.admin.version.dto.AdminVersionUpdateRequest;
import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "versions")
@NoArgsConstructor(access = PROTECTED)
public class Version extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "version", nullable = false, length = 50)
    private String version;

    @NotNull
    @Column(name = "type", length = 50)
    private String type;

    @Column(name = "title", length = 50)
    private String title;

    @OneToMany(mappedBy = "version", orphanRemoval = true, cascade = ALL, fetch = FetchType.EAGER)
    private List<VersionContent> contents = new ArrayList<>();

    @Column(name = "is_previous")
    private boolean isPrevious;

    @Builder
    private Version(
        Integer id,
        String type,
        String version,
        String title
    ) {
        this.id = id;
        this.type = type;
        this.version = version;
        this.title = title;
    }

    public void toPreviousVersion() {
        this.isPrevious = true;
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

    public static Version of(VersionType type, AdminVersionUpdateRequest request) {
        Version newVersion = Version.builder()
            .type(type.getValue())
            .version(request.version())
            .title(request.title())
            .build();

        List<VersionContent> contents = request.body().stream()
            .map(body -> body.toEntity(newVersion))
            .toList();
        newVersion.getContents().addAll(contents);

        return newVersion;
    }
}
