package in.koreatech.koin.global.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.springframework.aop.framework.AopProxyUtils;

import in.koreatech.koin.global.reflection.exception.ClassNotFoundException;
import in.koreatech.koin.global.reflection.exception.MethodNotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReflectionUtils {

    public static <A extends Annotation> Object findAnnotationValue(
        Object proxy,
        Class<A> annotationType,
        String methodName
    ) {
        Class<?> object = AopProxyUtils.ultimateTargetClass(proxy);
        try {
            A annotation = object.getDeclaredAnnotation(annotationType);
            return extractAnnotationValue(annotation, methodName);
        } catch (Exception e) {
            throw new ClassNotFoundException(String.format(
                "클래스가 어노테이션을 가지고 있지 않습니다. class: %s, annotation: %s",
                proxy,
                annotationType
            ));
        }
    }

    private static <A extends Annotation> Object extractAnnotationValue(
        A annotation,
        String methodName
    ) {
        try {
            Method method = annotation.annotationType().getMethod(methodName);
            return method.invoke(annotation);
        } catch (Exception e) {
            throw new MethodNotFoundException(String.format(
                "클래스에 메서드가 없습니다. class: %s, method: %s",
                annotation,
                methodName
            ));
        }
    }
}
