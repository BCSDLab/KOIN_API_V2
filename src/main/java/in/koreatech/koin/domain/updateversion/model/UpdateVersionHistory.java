package in.koreatech.koin.domain.updateversion.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "update_version_history")
@NoArgsConstructor(access = PROTECTED)
public class UpdateVersionHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private UpdateVersionType type;

    @Size(max = 50)
    @NotNull
    @Column(name = "version", nullable = false, length = 50)
    private String version;

    @Column(name = "title", length = 50)
    private String title;

    @Builder
    private UpdateVersionHistory(
        UpdateVersionType type,
        String version,
        String title
    ) {
        this.version = version;
        this.type = type;
        this.title = title;
    }

    public static UpdateVersionHistory from(UpdateVersion version) {
        return UpdateVersionHistory.builder()
            .type(version.getType())
            .version(version.getVersion())
            .title(version.getTitle())
            .build();
    }
}
