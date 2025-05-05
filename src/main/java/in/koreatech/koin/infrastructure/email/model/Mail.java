package in.koreatech.koin.infrastructure.email.model;

import org.thymeleaf.context.Context;

import lombok.Builder;

@Builder
public class Mail {

    private static final String CERTIFICATION_CODE = "certificationCode";

    private final String certificationCode;

    private final Context context = new Context();

    public Context convertToMap() {
        context.setVariable(CERTIFICATION_CODE, certificationCode);
        return context;
    }
}
