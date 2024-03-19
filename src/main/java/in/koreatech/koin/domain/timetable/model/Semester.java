package in.koreatech.koin.domain.timetable.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import static jakarta.persistence.GenerationType.IDENTITY;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import static lombok.AccessLevel.PROTECTED;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "semester")
@NoArgsConstructor(access = PROTECTED)
public class Semester {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Size(max = 10)
    @NotNull
    @Column(name = "semester", nullable = false)
    private String semester;

    @Builder
    private Semester(String semester) {
        this.semester = semester;
    }
}
