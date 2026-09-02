package in.koreatech.koin.global.code;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.method.HandlerMethod;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import in.koreatech.koin.global.exception.ErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import jakarta.validation.Constraint;
import jakarta.validation.Valid;

@Component
public class ApiResponseCodesOperationCustomizer implements OperationCustomizer {

    private static final String TRACE_ID_EXAMPLE = "123e4567-e89b-12d3-a456-426614174000";

    // 에러 스키마를 미리 생성하여 최적화
    private final Schema<?> errorSchema = loadSchema(ErrorResponse.class);
    private final SnakeCaseStrategy snake = new SnakeCaseStrategy();

    @Override
    public Operation customize(Operation operation, HandlerMethod handler) {
        ApiResponseCodes ann = handler.getMethodAnnotation(ApiResponseCodes.class);
        if (ann == null) {
            return operation;
        }

        ApiResponses responses = operation.getResponses();
        if (responses == null) {
            responses = new ApiResponses();
            operation.setResponses(responses);
        }
        ApiResponses targetResponses = responses;

        Type returnType = getActualResponseType(handler);
        Map<Integer, List<ApiResponseCode>> codesByStatus = Arrays.stream(ann.value())
            .collect(Collectors.groupingBy(
                code -> code.getHttpStatus().value(),
                LinkedHashMap::new,
                Collectors.toList()
            ));

        ApiResponse springdocSuccessResponse = responses.get("200");
        boolean hasExplicitSuccessResponse = codesByStatus.keySet().stream()
            .anyMatch(status -> status >= 200 && status < 300);
        if (hasExplicitSuccessResponse && !codesByStatus.containsKey(200)) {
            responses.remove("200");
        }

        codesByStatus.forEach((status, codes) -> {
            String statusKey = String.valueOf(status);
            ApiResponse existingResponse = targetResponses.get(statusKey);
            targetResponses.put(
                statusKey,
                createApiResponse(codes, handler, returnType, existingResponse, springdocSuccessResponse)
            );
        });

        return operation;
    }

    private Type getActualResponseType(HandlerMethod handler) {
        Type returnType = handler.getMethod().getGenericReturnType();

        if (returnType instanceof ParameterizedType parameterizedType) {
            if (parameterizedType.getRawType().equals(ResponseEntity.class)) {
                return parameterizedType.getActualTypeArguments()[0];
            }
        }
        return returnType;
    }

    private ApiResponse createApiResponse(
        List<ApiResponseCode> codes,
        HandlerMethod handler,
        Type returnType,
        ApiResponse existingResponse,
        ApiResponse springdocSuccessResponse
    ) {
        ApiResponse apiResponse = existingResponse == null ? new ApiResponse() : existingResponse;
        String description = codes.stream()
            .map(code -> code.getCode() + ": " + code.getMessage())
            .collect(Collectors.joining("\n"));
        apiResponse.setDescription(mergeDescriptions(apiResponse.getDescription(), description));

        ApiResponseCode firstCode = codes.get(0);
        if (firstCode.getHttpStatus().is2xxSuccessful()) {
            mergeSuccessResponse(apiResponse, springdocSuccessResponse, returnType, firstCode);
        } else {
            mergeErrorResponse(apiResponse, codes, handler);
        }
        return apiResponse;
    }

    private String mergeDescriptions(String existingDescription, String generatedDescription) {
        if (existingDescription == null || existingDescription.isBlank()) {
            return generatedDescription;
        }
        if (generatedDescription == null || generatedDescription.isBlank()
            || existingDescription.equals(generatedDescription)
            || existingDescription.endsWith("\n" + generatedDescription)) {
            return existingDescription;
        }
        return existingDescription + "\n" + generatedDescription;
    }

    private void mergeSuccessResponse(
        ApiResponse response,
        ApiResponse springdocSuccessResponse,
        Type returnType,
        ApiResponseCode code
    ) {
        if (isNoContentResponse(returnType) || code.getHttpStatus().value() == 204) {
            response.setContent(null);
            return;
        }
        if (hasSchema(response.getContent())) {
            return;
        }
        if (springdocSuccessResponse != null && hasSchema(springdocSuccessResponse.getContent())) {
            response.setContent(springdocSuccessResponse.getContent());
            return;
        }
        response.setContent(new Content().addMediaType(
            APPLICATION_JSON_VALUE,
            new MediaType().schema(loadSchema(returnType))
        ));
    }

    private boolean hasSchema(Content content) {
        return content != null && content.values().stream()
            .anyMatch(mediaType -> mediaType != null && mediaType.getSchema() != null);
    }

    private boolean isNoContentResponse(Type returnType) {
        return returnType.equals(Void.class) || returnType.equals(void.class);
    }

    private Map<String,Object> createErrorExample(
        ApiResponseCode code,
        HandlerMethod handler
    ) {
        if (code == ApiResponseCode.INVALID_REQUEST_BODY) {
            return createInvalidRequestBodyErrorExample(code, handler);
        }
        return Map.of(
            "code", code.getCode(),
            "message", code.getMessage(),
            "errorTraceId", TRACE_ID_EXAMPLE
        );
    }

    private Map<String,Object> createInvalidRequestBodyErrorExample(
        ApiResponseCode code,
        HandlerMethod handler
    ) {
        List<Map<String,Object>> fieldErrors = extractFieldErrors(handler);
        String firstMsg = fieldErrors.stream()
            .map(e -> (String)e.get("message"))
            .findFirst()
            .orElse(code.getMessage());

        Map<String,Object> example = new LinkedHashMap<>();
        example.put("code", code.getCode());
        example.put("message", firstMsg);
        example.put("errorTraceId", TRACE_ID_EXAMPLE);
        example.put("fieldErrors", fieldErrors);
        return example;
    }

    /**
     * 핸들러 메서드의 @RequestBody DTO 필드에 붙은
     * jakarta.validation 제약조건(@Constraint) 검증 어노테이션 정보를 꺼내,
     * JSON 객체 리스트로 반환합니다.
     */
    private List<Map<String,Object>> extractFieldErrors(HandlerMethod handler) {
        return Arrays.stream(handler.getMethodParameters())
            .filter(p -> p.hasParameterAnnotation(RequestBody.class))
            .findFirst()
            .map(MethodParameter::getParameterType)
            .map(this::extractFieldErrors)
            .orElse(List.of());
    }

    /**
     * DTO 클래스의 모든 필드를 탐색해서
     *  1) @Constraint 어노테이션이 붙은 경우 → 해당 필드 에러 추가
     *  2) @Valid 가 붙은 중첩 필드는 재귀 호출
     */
    private List<Map<String,Object>> extractFieldErrors(Class<?> dtoClass) {
        return Arrays.stream(dtoClass.getDeclaredFields())
            .flatMap(f -> {
                var selfErrors = Arrays.stream(f.getAnnotations())
                    .filter(a -> a.annotationType().isAnnotationPresent(Constraint.class))
                    .map(a -> fieldErrorEntry(f, a));

                if (f.isAnnotationPresent(Valid.class)) {
                    return Stream.concat(
                        selfErrors,
                        extractFieldErrors(f.getType()).stream()
                    );
                } else {
                    return selfErrors;
                }
            })
            .toList();
    }

    private Map<String,Object> fieldErrorEntry(Field field, Annotation ann) {
        String name = snake.translate(field.getName());
        String constraint = ann.annotationType().getSimpleName();
        String msg = getConstraintMessage(ann, field.getName());

        return Map.of(
            "field", name,
            "constraint", constraint,
            "message", msg
        );
    }

    /**
     * 검증 어노테이션의 message 값을 가져오고, 없을 경우 기본 문구를 사용합니다.
     */
    private String getConstraintMessage(Annotation ann, String fieldName) {
        try {
            Method m = ann.annotationType().getMethod("message");
            return (String)m.invoke(ann);
        } catch (Exception e) {
            return String.format("%s 입력값이 잘못 되었습니다.", fieldName);
        }
    }

    private static Schema<?> loadSchema(Type type) {
        if (type.equals(Void.class) || type.equals(void.class)) {
            return new Schema<>().type("object");
        }

        ResolvedSchema resolvedSchema = ModelConverters.getInstance().readAllAsResolvedSchema(type);
        if (resolvedSchema == null) {
            return new Schema<>().type("object");
        }

        return resolvedSchema.schema;
    }

    private void mergeErrorResponse(
        ApiResponse response,
        List<ApiResponseCode> codes,
        HandlerMethod handler
    ) {
        Content content = response.getContent();
        if (content == null) {
            content = new Content();
            response.setContent(content);
        }

        MediaType mediaType = content.get(APPLICATION_JSON_VALUE);
        if (mediaType == null) {
            mediaType = new MediaType();
            content.addMediaType(APPLICATION_JSON_VALUE, mediaType);
        }
        if (mediaType.getSchema() == null) {
            mediaType.setSchema(errorSchema);
        }

        Map<String, Example> examples = mediaType.getExamples() == null
            ? new LinkedHashMap<>()
            : new LinkedHashMap<>(mediaType.getExamples());
        moveSingleExampleToNamedExamples(mediaType, examples);
        for (ApiResponseCode code : codes) {
            examples.putIfAbsent(
                code.getCode(),
                new Example()
                    .summary(code.getCode())
                    .description(code.getMessage())
                    .value(createErrorExample(code, handler))
            );
        }
        mediaType.setExamples(examples);
    }

    private void moveSingleExampleToNamedExamples(MediaType mediaType, Map<String, Example> examples) {
        if (mediaType.getExample() == null) {
            return;
        }
        String exampleName = "default";
        int suffix = 2;
        while (examples.containsKey(exampleName)) {
            exampleName = "default-" + suffix++;
        }
        examples.put(exampleName, new Example().value(mediaType.getExample()));
        mediaType.setExample(null);
    }
}
