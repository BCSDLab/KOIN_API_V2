package in.koreatech.koin.domain.coopshop.model;

import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_SEMESTER_FORMAT;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_START_DATE_AFTER_END_DATE;
import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.global.exception.CustomException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "coop_semester")
@NoArgsConstructor(access = PROTECTED)
public class CoopSemester extends BaseEntity {

    private static final Pattern SEMESTER_PATTERN = Pattern.compile("^\\d{2}-.+$");

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "semester", nullable = false)
    private String semester;

    @NotNull
    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @NotNull
    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @NotNull
    @Column(name = "is_applied", columnDefinition = "TINYINT", nullable = false)
    private boolean isApplied = false;

    @OneToMany(mappedBy = "coopSemester", orphanRemoval = true, cascade = {PERSIST, REFRESH, MERGE, REMOVE},
        fetch = FetchType.EAGER)
    private List<CoopShop> coopShops = new ArrayList<>();

    @Builder
    private CoopSemester(
        String semester,
        LocalDate fromDate,
        LocalDate toDate
    ) {
        this.semester = semester;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public static CoopSemester of(String semester, LocalDate fromDate, LocalDate toDate) {
        validateDateRange(fromDate, toDate);
        validateSemesterFormat(semester);
        return CoopSemester.builder()
            .semester(semester)
            .fromDate(fromDate)
            .toDate(toDate)
            .build();
    }

    private static void validateSemesterFormat(String semester) {
        if (!SEMESTER_PATTERN.matcher(semester).matches()) {
            throw CustomException.of(INVALID_SEMESTER_FORMAT);
        }
    }

    private static void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate.isAfter(toDate)) {
            throw CustomException.of(INVALID_START_DATE_AFTER_END_DATE);
        }
    }

    public void updateApply(boolean isApplied) {
        this.isApplied = isApplied;
    }
}
