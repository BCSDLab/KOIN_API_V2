package in.koreatech.koin.domain.user.service.verification;

import java.util.regex.Pattern;

import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;

public class VerificationTypeDetector {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{11}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@koreatech.ac.kr$");

    public static String detect(String target) {
        if (PHONE_PATTERN.matcher(target).matches()) return "sms";
        if (EMAIL_PATTERN.matcher(target).matches()) return "email";

        throw new KoinIllegalArgumentException("유효하지 않은 이메일 또는 전화번호 형식입니다: " + target);
    }
}
