package in.koreatech.koin.domain.student.model;

import static jakarta.persistence.FetchType.*;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserIdentity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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

    @JoinColumn(name = "department_id")
    @ManyToOne(fetch = LAZY)
    private Department department;

    @JoinColumn(name = "major_id")
    @ManyToOne(fetch = LAZY)
    private Major major;

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
        User user,
        Major major
    ) {
        this.anonymousNickname = anonymousNickname;
        this.studentNumber = studentNumber;
        this.department = department;
        this.userIdentity = userIdentity;
        this.isGraduated = isGraduated;
        this.user = user;
        this.major = major;
    }

    public void updateInfo(String studentNumber, Department department) {
        this.studentNumber = studentNumber;
        this.department = department;
    }

    public void updateInfo(String studentNumber, Major major) {
        this.studentNumber = studentNumber;
        this.major = major;
    }

    public void updateStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public void updateDepartmentMajor(Department department, Major major) {
        this.department = department;
        this.major = major;
    }
}
