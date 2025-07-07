package in.koreatech.koin._common.code;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.method.HandlerMethod;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import in.koreatech.koin._common.exception.ErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import jakarta.validation.Constraint;

@Component
public class ApiResponseCodesOperationCustomizer implements OperationCustomizer {

    private static final String UUID_EXAMPLE = "123e4567-e89b-12d3-a456-426614174000";

    private final io.swagger.v3.oas.models.media.Schema<?> errorSchema = loadSchema(ErrorResponse.class);
    private final SnakeCaseStrategy snake = new SnakeCaseStrategy();

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        ApiResponseCodes mapping = handlerMethod.getMethodAnnotation(ApiResponseCodes.class);
        if (mapping == null) {
            return operation;
        }

        ApiResponses responses = operation.getResponses();
        responses.clear();

        Type returnType = handlerMethod.getMethod().getGenericReturnType();
        ApiResponseCode[] codes = mapping.value();

        for (int i = 0; i < codes.length; i++) {
            ApiResponseCode code = codes[i];
            String key = String.format("%d) %d", i + 1, code.getHttpStatus().value());
            responses.put(key,
                buildApiResponse(code.getMessage(),
                    () -> createMediaType(code, handlerMethod, returnType)));
        }
        return operation;
    }

    private ApiResponse buildApiResponse(String description, Supplier<MediaType> supplier) {
        return new ApiResponse()
            .description(description)
            .content(new Content().addMediaType(APPLICATION_JSON_VALUE, supplier.get()));
    }

    private MediaType createMediaType(ApiResponseCode code, HandlerMethod handlerMethod, Type returnType) {
        if (code.getHttpStatus().is2xxSuccessful()) {
            return new MediaType().schema(loadSchema(returnType));
        } else if (code.equals(ApiResponseCode.INVALID_REQUEST_BODY)) {
            return errorMediaType(buildInvalidPayloadExample(code, handlerMethod));
        } else {
            return errorMediaType(buildGenericErrorExample(code));
        }
    }

    private MediaType errorMediaType(Map<String, Object> example) {
        MediaType mt = new MediaType().schema(errorSchema);
        mt.example(example);
        return mt;
    }

    private Map<String, Object> buildInvalidPayloadExample(
        ApiResponseCode code,
        HandlerMethod handlerMethod
    ) {
        List<Map<String, Object>> fieldErrors = introspectFieldErrors(handlerMethod);
        String topMessage = getTopMessage(fieldErrors, code);

        Map<String, Object> ex = new LinkedHashMap<>();
        ex.put("code", code.getCode());
        ex.put("message", topMessage);
        ex.put("errorTraceId", UUID_EXAMPLE);
        ex.put("fieldErrors", fieldErrors + "(안드로이드 호환성 문제로 당분간 비활성화됩니다.)");
        return ex;
    }

    private String getTopMessage(List<Map<String, Object>> fieldErrors, ApiResponseCode code) {
        return fieldErrors.stream()
            .map(err -> (String)err.get("message"))
            .findFirst()
            .orElse(code.getMessage());
    }

    private List<Map<String, Object>> introspectFieldErrors(HandlerMethod handlerMethod) {
        return Arrays.stream(handlerMethod.getMethodParameters())
            .filter(p -> p.hasParameterAnnotation(RequestBody.class))
            .findFirst()
            .map(MethodParameter::getParameterType)
            .map(dto -> Arrays.stream(dto.getDeclaredFields())
                .flatMap(field -> Arrays.stream(field.getAnnotations())
                    .filter(a -> a.annotationType().isAnnotationPresent(Constraint.class))
                    .map(a -> toFieldErrorEntry(field, a)))
                .collect(Collectors.toList()))
            .orElse(List.of());
    }

    private Map<String, Object> toFieldErrorEntry(Field field, Annotation ann) {
        String name = snake.translate(field.getName());
        String constraint = ann.annotationType().getSimpleName();
        String message = extractConstraintMessage(ann, field.getName());

        return Map.of(
            "field", name,
            "constraint", constraint,
            "message", message
        );
    }

    private String extractConstraintMessage(Annotation ann, String fieldName) {
        try {
            Method m = ann.annotationType().getMethod("message");
            return (String)m.invoke(ann);
        } catch (Exception e) {
            return String.format("%s 입력값이 잘못 되었습니다.", fieldName);
        }
    }

    private Map<String, Object> buildGenericErrorExample(ApiResponseCode code) {
        return Map.of(
            "code", code.getCode(),
            "message", code.getMessage(),
            "errorTraceId", UUID_EXAMPLE
        );
    }

    private static io.swagger.v3.oas.models.media.Schema<?> loadSchema(Type type) {
        return ModelConverters.getInstance()
            .readAllAsResolvedSchema(type)
            .schema;
    }
}
