package in.koreatech.koin.global.config;


import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import in.koreatech.koin.domain.auth.AuthArgumentResolver;
import in.koreatech.koin.domain.auth.ExtractAuthenticationInterceptor;
import in.koreatech.koin.global.ipaddress.IpAddressArgumentResolver;
import in.koreatech.koin.global.ipaddress.IpAddressInterceptor;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final ExtractAuthenticationInterceptor extractAuthenticationInterceptor;
    private final IpAddressArgumentResolver ipAddressArgumentResolver;
    private final AuthArgumentResolver authArgumentResolver;
    private final IpAddressInterceptor ipAddressInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(extractAuthenticationInterceptor)
            .addPathPatterns("/**")
            .order(0);
        registry.addInterceptor(ipAddressInterceptor)
            .addPathPatterns("/**")
            .order(1);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authArgumentResolver);
        resolvers.add(ipAddressArgumentResolver);
    }
}
