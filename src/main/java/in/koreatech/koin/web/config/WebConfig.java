package in.koreatech.koin.web.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import in.koreatech.koin._common.auth.AuthArgumentResolver;
import in.koreatech.koin._common.auth.ExtractAuthenticationInterceptor;
import in.koreatech.koin._common.auth.UserIdArgumentResolver;
import in.koreatech.koin.integration.fcm.notification.controller.NotificationSubscribeTypeConverter;
import in.koreatech.koin.integration.s3.convertor.ImageUploadDomainEnumConverter;
import in.koreatech.koin.web.host.ServerURLArgumentResolver;
import in.koreatech.koin.web.host.ServerURLInterceptor;
import in.koreatech.koin.web.ipaddress.IpAddressArgumentResolver;
import in.koreatech.koin.web.ipaddress.IpAddressInterceptor;
import in.koreatech.koin.admin.abtest.useragent.UserAgentArgumentResolver;
import in.koreatech.koin.domain.bus.converter.BusStationEnumConverter;
import in.koreatech.koin.domain.bus.converter.BusTypeEnumConverter;
import in.koreatech.koin.domain.shop.dto.shop.ShopsFilterCriteriaConverter;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final ExtractAuthenticationInterceptor extractAuthenticationInterceptor;
    private final IpAddressArgumentResolver ipAddressArgumentResolver;
    private final ServerURLInterceptor serverURLInterceptor;

    private final AuthArgumentResolver authArgumentResolver;
    private final IpAddressInterceptor ipAddressInterceptor;
    private final ServerURLArgumentResolver serverURLArgumentResolver;
    private final UserIdArgumentResolver userIdArgumentResolver;
    private final UserAgentArgumentResolver userAgentArgumentResolver;

    private final CorsProperties corsProperties;

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
        resolvers.add(serverURLArgumentResolver);
        resolvers.add(userIdArgumentResolver);
        resolvers.add(userAgentArgumentResolver);
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new BusTypeEnumConverter());
        registry.addConverter(new BusStationEnumConverter());
        registry.addConverter(new ImageUploadDomainEnumConverter());
        registry.addConverter(new NotificationSubscribeTypeConverter());
        registry.addConverter(new ShopsFilterCriteriaConverter());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins(corsProperties.allowedOrigins().toArray(new String[0]))
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
            .addResourceLocations("classpath:/static/");
    }
}
