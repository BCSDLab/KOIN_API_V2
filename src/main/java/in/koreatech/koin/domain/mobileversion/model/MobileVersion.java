package in.koreatech.koin.domain.mobileversion.model;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
public class MobileVersion extends BaseEntity {

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

    @Column(name = "title", length = 50)
    private String title;

    @OneToMany(mappedBy = "version", orphanRemoval = true, cascade = ALL, fetch = FetchType.EAGER)
    private List<MobileVersionMessage> contents = new ArrayList<>();

    @Builder
    private MobileVersion(@NotNull String version, @NotNull String type) {
        this.version = version;
        this.type = type;
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
