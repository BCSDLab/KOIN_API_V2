package in.koreatech.koin.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import in.koreatech.koin.domain.bus.util.ExpressBusClient;
import in.koreatech.koin.global.domain.callcontol.model.BaseApi;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class CallControlConfig {

    @Bean(name = "ExpressBus")
    public BaseApi registerExpressBusConfig() {
        return generateBaseApi(ExpressBusClient.class);
    }

    private BaseApi generateBaseApi(Class<?> baseApiType) {
        return BaseApi.of(baseApiType);
    }
}
