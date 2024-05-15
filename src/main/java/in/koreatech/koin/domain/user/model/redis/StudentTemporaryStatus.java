package in.koreatech.koin.domain.user.model.redis;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.security.crypto.password.PasswordEncoder;

import in.koreatech.koin.domain.user.dto.StudentRegisterRequest;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.domain.user.model.UserIdentity;
import in.koreatech.koin.domain.user.model.UserType;
import lombok.Getter;

@Getter
@RedisHash(value = "StudentTemporaryStatus@")
public class StudentTemporaryStatus {

    private static final long CACHE_EXPIRE_SECOND = 60 * 60 * 1L;

    @Id
    private String key;

    private String email;

    private String name;

    private String password;

    private String nickname;

    private UserGender gender;

    private boolean isGraduated;

    private String department;

    private String studentNumber;

    private String phoneNumber;

    @TimeToLive
    private Long expiration;

    public StudentTemporaryStatus(String key, String email, String name, String password, String nickname,
        UserGender gender, boolean isGraduated, String department, String studentNumber, String phoneNumber) {
        this.key = key;
        this.email = email;
        this.name = name;
        this.password = password;
        this.nickname = nickname;
        this.gender = gender;
        this.isGraduated = isGraduated;
        this.department = department;
        this.studentNumber = studentNumber;
        this.phoneNumber = phoneNumber;
        this.expiration = CACHE_EXPIRE_SECOND;
    }

    public static StudentTemporaryStatus of(StudentRegisterRequest request, String authToken) {
        return new StudentTemporaryStatus(authToken, request.email(), request.name(), request.password(), request.nickname(), request.gender(),
            request.isGraduated(), request.department(), request.studentNumber(), request.phoneNumber());
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
            .authToken(key)
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
