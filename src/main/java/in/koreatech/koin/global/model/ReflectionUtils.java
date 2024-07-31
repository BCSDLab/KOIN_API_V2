package in.koreatech.koin.global.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.springframework.aop.framework.AopProxyUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReflectionUtils {

    public static <A extends Annotation> Object findAnnotationValue(Object proxy, Class<A> annotationType,
        String methodName) {
        Class<?> object = AopProxyUtils.ultimateTargetClass(proxy);
        try {
            A annotation = object.getDeclaredAnnotation(annotationType);
            return extractAnnotationValue(annotation, methodName);
        } catch (Exception e) {

            throw new IllegalArgumentException();
        }
    }

    private static <A extends Annotation> Object extractAnnotationValue(A annotation, String methodName) {
        try {
            Method method = annotation.annotationType().getMethod(methodName);
            return method.invoke(annotation);
        } catch (Exception e) {
            throw new RuntimeException("어노테이션 값 가져오기 실패: " + e.getMessage(), e);
        }
    }
}
