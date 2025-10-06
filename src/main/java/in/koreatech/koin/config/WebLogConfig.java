package in.koreatech.koin.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import in.koreatech.koin.global.logging.RequestLoggingFilter;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebLogConfig {

    private final RequestLoggingFilter requestLoggingFilter;

    @Bean
    public FilterRegistrationBean<RequestLoggingFilter> firstFilter() {
        FilterRegistrationBean<RequestLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        requestLoggingFilter.setIgnoredUrlPatterns(
            "/error",
            "/favicon.ico",
            "/api-docs/**",
            "/swagger-ui/**"
        );
        registrationBean.setFilter(requestLoggingFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);
        registrationBean.setName("requestLoggingFilter");
        return registrationBean;
    }
}
