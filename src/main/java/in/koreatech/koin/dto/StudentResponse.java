package in.koreatech.koin.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.model.User;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@JsonNaming(value = SnakeCaseStrategy.class)
public record StudentResponse(
    String anonymousNickname,
    String email,
    String gender,
    String major,
    String name,
    String nickname,
    String phoneNumber,
    String studentNumber
) {

    public static StudentResponse from(Student student) {
        User user = student.getUser();
        return new StudentResponse(
            student.getAnonymousNickname(),
            user.getEmail(),
            user.getGender().name(),
            student.getDepartment(),
            user.getName(),
            user.getNickname(),
            user.getPhoneNumber(),
            student.getStudentNumber()
        );
    }
}
