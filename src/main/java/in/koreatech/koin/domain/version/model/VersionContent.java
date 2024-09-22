package in.koreatech.koin.domain.version.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "version_contents")
@NoArgsConstructor(access = PROTECTED)
public class VersionContent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", referencedColumnName = "id", nullable = false)
    private Version version;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Builder
    private VersionContent(Version version, String title, String content) {
        this.version = version;
        this.title = title;
        this.content = content;
    }
}
