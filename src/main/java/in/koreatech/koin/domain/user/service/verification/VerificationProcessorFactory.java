package in.koreatech.koin.domain.user.service.verification;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin.domain.user.model.VerificationType;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VerificationProcessorFactory {

    private final List<VerificationProcessor> verificationProcessors;

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{11}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    public VerificationProcessor getProcessor(String phoneNumberOrEmail) {
        VerificationType verificationType = detect(phoneNumberOrEmail);
        return verificationProcessors.stream()
            .filter(processor -> processor.getType().equals(verificationType))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 인증 타입입니다: " + verificationType));
    }

    private VerificationType detect(String target) {
        if (PHONE_PATTERN.matcher(target).matches())
            return VerificationType.SMS;
        if (EMAIL_PATTERN.matcher(target).matches())
            return VerificationType.EMAIL;
        throw new KoinIllegalArgumentException("유효하지 않은 이메일 또는 전화번호 형식입니다");
    }
}
