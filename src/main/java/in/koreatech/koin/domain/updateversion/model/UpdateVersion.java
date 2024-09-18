package in.koreatech.koin.domain.updateversion.model;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;

import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "update_versions")
@NoArgsConstructor(access = PROTECTED)
public class UpdateVersion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "version", nullable = false, length = 50)
    private String version;

    @NotNull
    @Column(name = "type", unique = true)
    @Enumerated(EnumType.STRING)
    private UpdateVersionType type;

    @Column(name = "title", length = 50)
    private String title;

    @OneToMany(mappedBy = "type", orphanRemoval = true, cascade = ALL, fetch = FetchType.EAGER)
    private List<UpdateContent> contents = new ArrayList<>();

    @Builder
    private UpdateVersion(
        Integer id,
        UpdateVersionType type,
        String version,
        String title,
        List<UpdateContent> contents
    ) {
        this.id = id;
        this.type = type;
        this.version = version;
        this.title = title;
        this.contents = contents;
    }

    public void update(String version, String title, List<UpdateContent> contents) {
        this.version = version;
        this.title = title;
        this.contents.addAll(contents);
    }
}
