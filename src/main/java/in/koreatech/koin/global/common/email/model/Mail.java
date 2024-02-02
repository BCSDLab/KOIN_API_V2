package in.koreatech.koin.global.common.email.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.VelocityContext;

import lombok.Builder;

@Builder
public class Mail {

    private static final String CERTIFICATION_CODE = "certification-code";

    private final String certificationCode;

    private final Map<String, Object> model = new HashMap<>();
    private final VelocityContext velocityContext = new VelocityContext();

    public VelocityContext convertToMap() {
        velocityContext.put(CERTIFICATION_CODE, certificationCode);
        return velocityContext;
    }
}
