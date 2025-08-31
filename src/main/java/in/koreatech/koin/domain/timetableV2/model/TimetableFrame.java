package in.koreatech.koin.domain.timetableV2.model;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Where;

import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.common.model.BaseEntity;
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
@Where(clause = "is_deleted=0")
@NoArgsConstructor(access = PROTECTED)
public class TimetableFrame extends BaseEntity {

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
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "is_main", nullable = false)
    private boolean isMain = false;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @OneToMany(mappedBy = "timetableFrame", orphanRemoval = true, cascade = ALL)
    private List<TimetableLecture> timetableLectures;

    @Builder
    public TimetableFrame(
        User user,
        Semester semester,
        String name,
        boolean isMain,
        List<TimetableLecture> timetableLectures,
        boolean isDeleted
    ) {
        this.user = user;
        this.semester = semester;
        this.name = name;
        this.isMain = isMain;
        this.timetableLectures = timetableLectures;
        this.isDeleted = isDeleted;
    }

    private static final String DEFAULT_TIMETABLE_FRAME_NAME = "시간표%d";

    public void renameAndSetMain(String name, boolean isMain) {
        this.name = name;
        this.isMain = isMain;
    }

    public void setMain(boolean isMain) {
        this.isMain = isMain;
    }

    public void addTimeTableLecture(TimetableLecture lecture) {
        if (timetableLectures == null) {
            timetableLectures = new ArrayList<>();
        }
        timetableLectures.add(lecture);
    }

    public void delete() {
        this.isDeleted = true;
        if (timetableLectures != null) {
            timetableLectures.forEach(TimetableLecture::delete);
        }
    }

    public void undelete() {
        this.isDeleted = false;
    }

    public void cancelMain() { this.isMain = false; }

    public static boolean isMainFrame(int currentFrameCount) {
        return currentFrameCount == 0;
    }

    public static String getTimetableName(String requestName, int currentFrameCount) {
        return (requestName != null) ? requestName : getDefaultTimetableFrameName(currentFrameCount + 1);
    }

    private static String getDefaultTimetableFrameName(int currentFrameCount) {
        return String.format(DEFAULT_TIMETABLE_FRAME_NAME, currentFrameCount);
    }
}
