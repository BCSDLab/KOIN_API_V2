package in.koreatech.koin.global.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import in.koreatech.koin.global.domain.callcontol.exception.BeanNotFoundException;
import in.koreatech.koin.global.domain.callcontol.exception.MethodNotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReflectionUtils {

    public static final ApplicationContext context = ApplicationContextProvider.getApplicationContext();

    public static List<?> getSubTypeBeans(Class<?> baseType) {
        try {
            return context.getBeansOfType(baseType).values().stream().toList();
        } catch (NoSuchBeanDefinitionException e) {
            throw new BeanNotFoundException("하위 타입이 존재하지 않습니다. 상위 타입: " + baseType);
        }
    }

    public static Object findBeanByName(String name) {
        return context.getBean(name);
    }

    public static Method findMethodByAnnotation(Object proxy, Class<? extends Annotation> annotationType) {
        Class<?> object = AopProxyUtils.ultimateTargetClass(proxy);
        return Arrays.stream(object.getDeclaredMethods())
            .filter(method -> method.isAnnotationPresent(annotationType))
            .findAny()
            .orElseThrow(() -> new MethodNotFoundException(
                "클래스 내에 다음 어노테이션을 가지는 메서드가 없습니다. annotation: " + annotationType
            ));
    }

    public static void callMethod(Object targetClass, Method targetMethod) {
        try {
            targetMethod.invoke(targetClass);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Api 호출에 실패하였습니다. message: " + e.getMessage());
        }
    }
}
