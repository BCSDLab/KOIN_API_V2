package in.koreatech.koin.domain.mobileversion.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.Clock;
import java.time.LocalDate;

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
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "version_message")
@NoArgsConstructor(access = PROTECTED)
public class MobileVersionMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", referencedColumnName = "id", nullable = false)
    private MobileVersion version;

    @NotNull
    @Column(name = "title", length = 50, unique = true)
    private String title;

    @NotNull
    @Column(name = "content", length = 50, unique = true)
    private String content;

    @Builder
    private MobileVersionMessage(@NotNull String version, @NotNull String type) {
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
