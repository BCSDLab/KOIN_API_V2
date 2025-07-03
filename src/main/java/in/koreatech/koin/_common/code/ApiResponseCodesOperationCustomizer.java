package in.koreatech.koin._common.code;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import in.koreatech.koin._common.exception.ErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

@Component
public class ApiResponseCodesOperationCustomizer implements OperationCustomizer {

    private static final String UUID_EXAMPLE = "123e4567-e89b-12d3-a456-426614174000 (예시값 - UUID)";
    private static final String STUDENT_NUMBER_EXAMPLE_FIELD = "loginId (예시값 - CamelCase 사용)";
    private static final String STUDENT_NUMBER_EXAMPLE_MESSAGE = "학번의 길이는 최대 10자입니다. (예시값 - 하위호환성 유지)";
    private static final String STUDENT_NUMBER_EXAMPLE_CONSTRAINT = "Size (예시값)";

    private final Schema<?> errorSchema = loadSchema(ErrorResponse.class);

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
            String key = String.format("%d) %s", i + 1, code.getHttpStatus().value());

            if (code.getHttpStatus().is2xxSuccessful()) {
                responses.put(key, buildApiResponse(
                    code.getMessage(),
                    () -> new MediaType().schema(loadSchema(returnType))
                ));
            } else if (code.equals(ApiResponseCode.INVALID_REQUEST_PAYLOAD)) {
                responses.put(key, buildApiResponse(
                    code.getMessage(),
                    () -> {
                        MediaType mt = new MediaType().schema(errorSchema);
                        mt.example(invalidRequestPayloadErrorExample(code));
                        return mt;
                    }
                ));
            } else {
                responses.put(key, buildApiResponse(
                    code.getMessage(),
                    () -> {
                        MediaType mt = new MediaType().schema(errorSchema);
                        mt.example(genericErrorExample(code));
                        return mt;
                    }
                ));
            }
        }

        return operation;
    }

    private ApiResponse buildApiResponse(String description, Supplier<MediaType> mediaTypeSupplier) {
        return new ApiResponse()
            .description(description)
            .content(new Content().addMediaType(
                APPLICATION_JSON_VALUE,
                mediaTypeSupplier.get()
            ));
    }

    private Map<String, Object> invalidRequestPayloadErrorExample(ApiResponseCode code) {
        Map<String, Object> ex = new LinkedHashMap<>();
        ex.put("code", code.getCode());
        ex.put("message", STUDENT_NUMBER_EXAMPLE_MESSAGE);
        ex.put("errorTraceId", UUID_EXAMPLE);
        ex.put("fieldErrors", List.of(Map.of(
            "field", STUDENT_NUMBER_EXAMPLE_FIELD,
            "message", STUDENT_NUMBER_EXAMPLE_MESSAGE,
            "constraint", STUDENT_NUMBER_EXAMPLE_CONSTRAINT
        )));
        return ex;
    }

    private Map<String, Object> genericErrorExample(ApiResponseCode code) {
        Map<String, Object> ex = new LinkedHashMap<>();
        ex.put("code", code.getCode());
        ex.put("message", code.getMessage());
        ex.put("errorTraceId", UUID_EXAMPLE);
        return ex;
    }

    private Schema<?> loadSchema(Type type) {
        return ModelConverters.getInstance()
            .readAllAsResolvedSchema(type)
            .schema;
    }
}
