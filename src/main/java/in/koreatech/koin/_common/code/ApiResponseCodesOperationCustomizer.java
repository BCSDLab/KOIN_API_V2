package in.koreatech.koin._common.code;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.lang.reflect.Type;
import java.util.Map;

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
        ApiResponseCode[] codes = mapping.value();  // 배열로 받아서

        for (int i = 0; i < codes.length; i++) {
            ApiResponseCode code = codes[i];
            String key = String.format("%d) %s", i + 1, code.getHttpStatus().value());
            if (code.getHttpStatus().is2xxSuccessful()) {
                responses.put(key, convertDataResponse(code, returnType));
            } else {
                responses.put(key, convertErrorResponse(code));
            }
        }

        return operation;
    }

    private Schema<?> loadSchema(Type type) {
        return ModelConverters.getInstance()
            .readAllAsResolvedSchema(type)
            .schema;
    }

    private ApiResponse convertErrorResponse(ApiResponseCode code) {
        MediaType mt = new MediaType()
            .schema(errorSchema)
            .example(Map.of(
                "code", code.getCode(),
                "message", code.getMessage(),
                "errorTraceId", "123e4567-e89b-12d3-a456-426614174000 (UUID 예시값)"
            ));

        return new ApiResponse()
            .description(code.getMessage())
            .content(new Content().addMediaType(APPLICATION_JSON_VALUE, mt));
    }

    private ApiResponse convertDataResponse(ApiResponseCode code, Type dtoType) {
        Schema<?> schema = loadSchema(dtoType);

        return new ApiResponse()
            .description(code.getMessage())
            .content(new Content().addMediaType(APPLICATION_JSON_VALUE, new MediaType().schema(schema)));
    }
}
