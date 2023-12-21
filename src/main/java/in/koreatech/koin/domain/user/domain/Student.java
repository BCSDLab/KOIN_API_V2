package in.koreatech.koin.domain.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@Entity
@Table(name = "students")
public class Student {

    @Id
    private Long userId;

    @Size(max = 255)
    @Column(name = "anonymous_nickname")
    private String anonymousNickname = "익명_" + System.currentTimeMillis();

    @Size(max = 20)
    @Column(name = "student_number", length = 20)
    private String studentNumber;

    @Size(max = 50)
    @Column(name = "major", length = 50)
    private String department;

    @Column(name = "identity")
    @Enumerated(EnumType.STRING)
    private UserIdentity userIdentity;

    @Column(name = "is_graduated")
    private Boolean isGraduated;
}
