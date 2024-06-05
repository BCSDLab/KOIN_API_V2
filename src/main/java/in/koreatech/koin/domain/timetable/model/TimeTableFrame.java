package in.koreatech.koin.domain.timetable.model;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.util.List;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "timetable_frame",
    indexes = @Index(name = "timetable_frame_INDEX", columnList = "user_id, semester_id")
)
@NoArgsConstructor(access = PROTECTED)
public class TimeTableFrame extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @NotNull
    @Size(max = 255)
    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @NotNull
    @Column(name = "is_main", nullable = false)
    private boolean isMain;

    @OneToMany(mappedBy = "timetableFrame", orphanRemoval = true, cascade = ALL)
    private List<TimeTableLecture> timeTableLectures;

    @Builder
    private TimeTableFrame(
        User user,
        Semester semester,
        String name,
        boolean isMain,
        List<TimeTableLecture> timeTableLectures
    ) {
        this.user = user;
        this.semester = semester;
        this.name = name;
        this.isMain = isMain;
        this.timeTableLectures = timeTableLectures;
    }
}
