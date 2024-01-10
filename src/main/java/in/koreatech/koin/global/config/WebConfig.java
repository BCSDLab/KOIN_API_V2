package in.koreatech.koin.global.config;

import in.koreatech.koin.domain.auth.resolver.StudentArgumentResolver;
import in.koreatech.koin.domain.auth.resolver.UserArgumentResolver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final UserArgumentResolver userArgumentResolver;
    private final StudentArgumentResolver studentArgumentResolver;

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userArgumentResolver);
        resolvers.add(studentArgumentResolver);
    }
}
