package in.koreatech.koin.domain.owner.model;

public record OwnerInVerification(
    String email,
    String certificationCode
) {

    public static OwnerInVerification from(String email, String certificationCode) {
        return new OwnerInVerification(email, certificationCode);
    }
}
