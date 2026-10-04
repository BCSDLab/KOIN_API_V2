package in.koreatech.koin.domain.dining.model;

import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "dining_soldout_report_sequence")
@NoArgsConstructor(access = PROTECTED)
public class DiningReportSequence {

    @Id
    @Column(columnDefinition = "TINYINT")
    private Integer id;

    @Column(name = "last_sequence", nullable = false)
    private Long lastSequence;

    public long next() {
        lastSequence = Math.addExact(lastSequence, 1L);
        return lastSequence;
    }
}
