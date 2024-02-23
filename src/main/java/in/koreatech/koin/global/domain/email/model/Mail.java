package in.koreatech.koin.global.domain.email.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.VelocityContext;

import lombok.Builder;

@Builder
public class Mail {

    private static final String CERTIFICATION_CODE = "certificationCode";

    private final String certificationCode;

    private final Map<String, Object> model = new HashMap<>();
    private final VelocityContext velocityContext = new VelocityContext();

    public VelocityContext convertToMap() {
        velocityContext.put(CERTIFICATION_CODE, certificationCode);
        return velocityContext;
    }
}
