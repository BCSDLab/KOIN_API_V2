package in.koreatech.koin.global.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import in.koreatech.koin.domain.bus.controller.BusStationEnumConverter;
import in.koreatech.koin.domain.bus.controller.BusTypeEnumConverter;
import in.koreatech.koin.global.auth.AuthArgumentResolver;
import in.koreatech.koin.global.auth.ExtractAuthenticationInterceptor;
import in.koreatech.koin.global.auth.UserIdArgumentResolver;
import in.koreatech.koin.global.domain.upload.controller.ImageUploadDomainEnumConverter;
import in.koreatech.koin.global.host.ServerURLArgumentResolver;
import in.koreatech.koin.global.host.ServerURLInterceptor;
import in.koreatech.koin.global.ipaddress.IpAddressArgumentResolver;
import in.koreatech.koin.global.ipaddress.IpAddressInterceptor;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final ExtractAuthenticationInterceptor extractAuthenticationInterceptor;
    private final IpAddressArgumentResolver ipAddressArgumentResolver;
    private final UserIdArgumentResolver userIdArgumentResolver;
    private final AuthArgumentResolver authArgumentResolver;
    private final IpAddressInterceptor ipAddressInterceptor;
    private final ServerURLArgumentResolver serverURLArgumentResolver;
    private final ServerURLInterceptor serverURLInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(extractAuthenticationInterceptor)
            .addPathPatterns("/**")
            .order(0);
        registry.addInterceptor(ipAddressInterceptor)
            .addPathPatterns("/**")
            .order(1);
        registry.addInterceptor(serverURLInterceptor)
            .addPathPatterns("/**")
            .order(2);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authArgumentResolver);
        resolvers.add(ipAddressArgumentResolver);
        resolvers.add(userIdArgumentResolver);
        resolvers.add(serverURLArgumentResolver);
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new BusTypeEnumConverter());
        registry.addConverter(new BusStationEnumConverter());
        registry.addConverter(new ImageUploadDomainEnumConverter());
    }
}
