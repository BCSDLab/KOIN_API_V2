package in.koreatech.koin.admin.user.dto;

import java.time.LocalDateTime;

import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.model.User;

public record AdminStudentResponse (
    Integer id,
    String nickname,
    String name,
    String phoneNumber,
    String userType,
    String email,
    Integer gender,
    Boolean isAuthed,
    LocalDateTime lastLoggedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String anonymousNickname,
    String studentNumber,
    String major,
    Boolean isGraduated
) {
    public static AdminStudentResponse from(Student student) {
        User user = student.getUser();
        return new AdminStudentResponse(
            user.getId(),
            user.getNickname(),
            user.getName(),
            user.getPhoneNumber(),
            user.getUserType().toString(),
            user.getEmail(),
            user.getGender().ordinal(),
            user.isAuthed(),
            user.getLastLoggedAt(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            student.getAnonymousNickname(),
            student.getStudentNumber(),
            student.getDepartment(),
            student.isGraduated()
        );
    }
}
