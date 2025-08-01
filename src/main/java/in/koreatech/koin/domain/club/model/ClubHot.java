package in.koreatech.koin.domain.club.model;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDate;

import in.koreatech.koin.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "club_hot")
@NoArgsConstructor(access = PROTECTED)
public class ClubHot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @JoinColumn(name = "club_id")
    @ManyToOne(fetch = LAZY)
    private Club club;

    @NotNull
    @Column(name = "ranking", nullable = false)
    private Integer ranking;

    @NotNull
    @Column(name = "period_hits", nullable = false, columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer periodHits = 0;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Builder
    private ClubHot(
        Integer id,
        Club club,
        Integer ranking,
        Integer periodHits,
        LocalDate startDate,
        LocalDate endDate
    ) {
        this.id = id;
        this.club = club;
        this.ranking = ranking;
        this.periodHits = periodHits;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
