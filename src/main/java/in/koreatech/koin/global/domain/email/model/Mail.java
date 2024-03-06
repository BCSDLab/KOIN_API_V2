package in.koreatech.koin.global.domain.email.model;

import java.util.HashMap;
import java.util.Map;

import org.thymeleaf.context.Context;

import lombok.Builder;

@Builder
public class Mail {

    private static final String CERTIFICATION_CODE = "certificationCode";

    private final String certificationCode;

    private final Map<String, Object> model = new HashMap<>();
    private final Context context = new Context();

    public Context convertToMap() {
        context.setVariable(CERTIFICATION_CODE, certificationCode);
        return context;
    }
}
