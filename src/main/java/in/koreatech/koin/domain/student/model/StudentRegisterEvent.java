package in.koreatech.koin.domain.student.model;

public record StudentRegisterEvent(
    String email,
    Integer studentId
) {

}
