package in.koreatech.koin.mcp.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import in.koreatech.koin.mcp.tool.EndpointSpecTools;

@Configuration
@ConditionalOnProperty(name = "spring.ai.mcp.server.enabled", havingValue = "true")
public class McpToolConfig {

    @Bean
    public ToolCallbackProvider endpointSpecToolCallbackProvider(EndpointSpecTools endpointSpecTools) {
        return MethodToolCallbackProvider.builder()
            .toolObjects(endpointSpecTools)
            .build();
    }
}
