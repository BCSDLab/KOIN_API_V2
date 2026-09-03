package in.koreatech.koin.global.code;

import java.lang.reflect.Method;
import java.util.Map;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import io.swagger.v3.oas.models.Operation;

@Component
public class DeprecationOperationCustomizer implements OperationCustomizer {

    public static final String EXTENSION_SINCE = "x-deprecated-since";
    public static final String EXTENSION_REASON = "x-deprecated-reason";
    public static final String EXTENSION_REPLACED_BY = "x-replaced-by";
    public static final String EXTENSION_FOR_REMOVAL = "x-for-removal";

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        Deprecation deprecation = findDeprecation(handlerMethod);
        if (deprecation == null) {
            return operation;
        }

        operation.setDeprecated(true);
        addIfNotBlank(operation, EXTENSION_SINCE, deprecation.since());
        addIfNotBlank(operation, EXTENSION_REASON, deprecation.reason());
        if (!deprecation.replacedByMethod().isBlank() || !deprecation.replacedByPath().isBlank()) {
            operation.addExtension(EXTENSION_REPLACED_BY, Map.of(
                "method", deprecation.replacedByMethod(),
                "path", deprecation.replacedByPath()
            ));
        }
        operation.addExtension(EXTENSION_FOR_REMOVAL, deprecation.forRemoval());
        return operation;
    }

    private void addIfNotBlank(Operation operation, String name, String value) {
        if (!value.isBlank()) {
            operation.addExtension(name, value);
        }
    }

    private Deprecation findDeprecation(HandlerMethod handlerMethod) {
        Deprecation deprecation = handlerMethod.getMethodAnnotation(Deprecation.class);
        if (deprecation != null) {
            return deprecation;
        }

        Method controllerMethod = handlerMethod.getMethod();
        for (Class<?> apiInterface : handlerMethod.getBeanType().getInterfaces()) {
            Deprecation interfaceDeprecation = findInterfaceMethodDeprecation(apiInterface, controllerMethod);
            if (interfaceDeprecation != null) {
                return interfaceDeprecation;
            }
        }
        return null;
    }

    private Deprecation findInterfaceMethodDeprecation(Class<?> apiInterface, Method controllerMethod) {
        for (Method interfaceMethod : apiInterface.getMethods()) {
            if (hasSameSignature(interfaceMethod, controllerMethod)) {
                return interfaceMethod.getAnnotation(Deprecation.class);
            }
        }
        return null;
    }

    private boolean hasSameSignature(Method left, Method right) {
        if (!left.getName().equals(right.getName())) {
            return false;
        }
        Class<?>[] leftTypes = left.getParameterTypes();
        Class<?>[] rightTypes = right.getParameterTypes();
        if (leftTypes.length != rightTypes.length) {
            return false;
        }
        for (int i = 0; i < leftTypes.length; i++) {
            if (!leftTypes[i].equals(rightTypes[i])) {
                return false;
            }
        }
        return true;
    }
}
