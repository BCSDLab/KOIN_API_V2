package in.koreatech.koin.domain.graduation.model;

import com.google.firebase.database.annotations.NotNull;
import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Table(name = "detect_graduation_calculation")
@NoArgsConstructor(access = PROTECTED)
public class DetectGraduationCalculation {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "is_changed", columnDefinition = "TINYINT")
    private boolean isChanged = false;

    public void updatedIsChanged(boolean isChanged) {
        this.isChanged = isChanged;
    }
}
