package in.koreatech.koin.domain.timetable.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;

import in.koreatech.koin.domain.timetableV3.model.Term;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "semester")
@NoArgsConstructor(access = PROTECTED)
public class Semester {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Size(max = 10)
    @NotNull
    @Column(name = "semester", nullable = false, unique = true)
    private String semester;

    @NotNull
    @Column(name = "year", nullable = false)
    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(name = "term", nullable = false, length = 20)
    private Term term;

    @Builder
    public Semester(String semester, Integer year, Term term) {
        this.semester = semester;
        this.year = year;
        this.term = term;
    }
}
