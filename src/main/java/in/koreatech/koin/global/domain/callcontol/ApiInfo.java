package in.koreatech.koin.global.domain.callcontol;

import java.lang.reflect.Method;

import lombok.Builder;

@Builder
public record ApiInfo (
    Object targetClass,
    Method targetMethod,
    int start,
    int end
) {

}
