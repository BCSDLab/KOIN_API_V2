package in.koreatech.koin.domain.user.model;

import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.domain.graduation.model.Department;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "students")
@NoArgsConstructor(access = PROTECTED)
public class Student {

    @Id
    @Column(name = "user_id")
    private Integer id;

    @Size(max = 255)
    @Column(name = "anonymous_nickname", unique = true)
    private String anonymousNickname = "익명_" + System.currentTimeMillis();

    @Size(max = 20)
    @Column(name = "student_number", length = 20)
    private String studentNumber;

    @Column(name = "major", length = 50)
    @ManyToOne(fetch = FetchType.LAZY)
    private Department department;

    @Column(name = "identity", columnDefinition = "SMALLINT")
    @Enumerated(EnumType.ORDINAL)
    private UserIdentity userIdentity;

    @Column(name = "is_graduated")
    private boolean isGraduated;

    @OneToOne
    @MapsId
    private User user;

    @Builder
    private Student(
        String anonymousNickname,
        String studentNumber,
        Department department,
        UserIdentity userIdentity,
        boolean isGraduated,
        User user
    ) {
        this.anonymousNickname = anonymousNickname;
        this.studentNumber = studentNumber;
        this.department = department;
        this.userIdentity = userIdentity;
        this.isGraduated = isGraduated;
        this.user = user;
    }

    public void update(String studentNumber, Department department) {
        this.studentNumber = studentNumber;
        this.department = department;
    }

    public static Integer parseStudentNumberYear(String studentNumber) {
        return Integer.parseInt(studentNumber.substring(0, 4));
    }
}
