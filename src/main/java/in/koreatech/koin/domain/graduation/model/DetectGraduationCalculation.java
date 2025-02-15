package in.koreatech.koin.domain.graduation.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import com.google.firebase.database.annotations.NotNull;

import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Builder
    private DetectGraduationCalculation(
        User user,
        boolean isChanged
    ) {
        this.user = user;
        this.isChanged = isChanged;
    }

    public void updatedIsChanged(boolean isChanged) {
        this.isChanged = isChanged;
    }
}
