package in.koreatech.koin.domain.student.model.redis;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.security.crypto.password.PasswordEncoder;

import in.koreatech.koin.domain.student.dto.RegisterStudentRequest;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Major;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.domain.user.model.UserIdentity;
import in.koreatech.koin.domain.user.model.UserType;
import lombok.Getter;

/**
 * 과거 사용자 회원가입 시 메일 인증 전 사용하던 캐시입니다.
 * 2025년 상반기 유저팀 스프린트 이후 더이상 사용되지 않을 예정입니다.
 * 강제 업데이트 후 삭제 예정입니다.
 */
@Getter
@RedisHash(value = "StudentTemporaryStatus")
public class UnAuthenticatedStudentInfo {

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

    public UnAuthenticatedStudentInfo(
        String email,
        String authToken,
        String nickname,
        String name,
        String password,
        UserGender gender,
        boolean isGraduated,
        String department,
        String studentNumber,
        String phoneNumber
    ) {
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

    public static UnAuthenticatedStudentInfo of(RegisterStudentRequest request, String authToken) {
        /* TODO : 강업 시 DTO 검증 변경 및 해당 로직 삭제
        이전 회원가입 시 전화번호에 하이픈을 붙여 저장합니다.
        DB는 전화번호에 하이픈을 붙이지 않고 저장합니다.
        DB 정합성을 위해 임시로 작성한 코드입니다.
         */
        String normalizedPhoneNumber = request.phoneNumber() == null ? null : request.phoneNumber().replaceAll("-", "");
        return new UnAuthenticatedStudentInfo(
            request.email(),
            authToken,
            request.nickname(),
            request.name(),
            request.password(),
            request.gender(),
            request.isGraduated(),
            request.major(),
            request.studentNumber(),
            normalizedPhoneNumber
        );
    }

    public Student toStudent(PasswordEncoder passwordEncoder, Department department, Major major) {
        String userId = email.substring(0, email.indexOf("@"));
        User user = User.builder()
            .password(passwordEncoder.encode(password))
            .email(email)
            .userId(userId)
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
            .major(major)
            .build();
    }
}
