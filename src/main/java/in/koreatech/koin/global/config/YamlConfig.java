package in.koreatech.koin.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yaml.snakeyaml.Yaml;

@Configuration
public class YamlConfig {

    @Bean
    public Yaml yaml() {
        return new Yaml();
    }
}
