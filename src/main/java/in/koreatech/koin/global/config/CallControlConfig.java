package in.koreatech.koin.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import in.koreatech.koin.domain.bus.util.ExpressBusClient;
import in.koreatech.koin.global.domain.callcontoller.model.ParentApi;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class CallControlConfig {

    @Bean(name = "ExpressBus")
    public ParentApi registerExpressBusConfig() {
        return generateParentApi(ExpressBusClient.class);
    }

    private ParentApi generateParentApi(Class<?> parentType) {
        return ParentApi.of(parentType);
    }
}
