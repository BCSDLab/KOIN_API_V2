package in.koreatech.koin.domain.student.model.redis;

import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.dto.StudentRegisterRequest;
import in.koreatech.koin.domain.user.model.*;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@RedisHash(value = "StudentTemporaryStatus")
public class StudentTemporaryStatus {

    private static final long CACHE_EXPIRE_SECOND = 60 * 60 * 10L;

    @Id
    @Indexed
    private String email;

    @Indexed
    private String authToken;

    @Indexed
    private String nickname;

    private String name;

    private String password;

    private UserGender gender;

    private boolean isGraduated;

    private String department;

    private String studentNumber;

    private String phoneNumber;

    @TimeToLive
    private Long expiration;

    public StudentTemporaryStatus(String email, String authToken, String nickname, String name, String password,
                                  UserGender gender, boolean isGraduated, String department, String studentNumber, String phoneNumber) {
        this.email = email;
        this.authToken = authToken;
        this.nickname = nickname;
        this.name = name;
        this.password = password;
        this.gender = gender;
        this.isGraduated = isGraduated;
        this.department = department;
        this.studentNumber = studentNumber;
        this.phoneNumber = phoneNumber;
        this.expiration = CACHE_EXPIRE_SECOND;
    }

    public static StudentTemporaryStatus of(StudentRegisterRequest request, String authToken) {
        return new StudentTemporaryStatus(request.email(), authToken, request.nickname(), request.name(), request.password(), request.gender(),
                request.isGraduated(), request.major(), request.studentNumber(), request.phoneNumber());
    }

    public Student toStudent(PasswordEncoder passwordEncoder) {
        User user = User.builder()
                .password(passwordEncoder.encode(password))
                .email(email)
                .name(name)
                .nickname(nickname)
                .gender(gender)
                .phoneNumber(phoneNumber)
                .isAuthed(true)
                .isDeleted(false)
                .userType(UserType.STUDENT)
                .build();

        return Student.builder()
                .user(user)
                .anonymousNickname("익명_" + (System.currentTimeMillis()))
                .isGraduated(isGraduated)
                .userIdentity(UserIdentity.UNDERGRADUATE)
                .department(department)
                .studentNumber(studentNumber)
                .build();
    }
}
