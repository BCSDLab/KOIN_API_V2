package in.koreatech.koin.domain.member.model;

import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "tracks")
@NoArgsConstructor(access = PROTECTED)
public class Track extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @NotNull
    @Column(name = "headcount", nullable = false)
    private Integer headcount;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Builder
    private Track(String name, Integer headcount, boolean isDeleted) {
        this.name = name;
        this.headcount = headcount != null ? headcount : 0;
        this.isDeleted = isDeleted;
    }

    public void update(String name, Integer headcount, boolean isDeleted) {
        this.name = name;
        this.headcount = headcount;
        this.isDeleted = isDeleted;
    }

    public void delete() {
        this.isDeleted = true;
    }
}
