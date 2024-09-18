package in.koreatech.koin.domain.updateversion.model;

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
@Table(name = "update_contents")
@NoArgsConstructor(access = PROTECTED)
public class UpdateContent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", referencedColumnName = "id", nullable = false)
    private UpdateVersion type;

    @Column(name = "title", length = 50)
    private String title;

    @Column(name = "content", length = 50)
    private String content;

    @Builder
    private UpdateContent(UpdateVersion type, String title, String content) {
        this.type = type;
        this.title = title;
        this.content = content;
    }
}
