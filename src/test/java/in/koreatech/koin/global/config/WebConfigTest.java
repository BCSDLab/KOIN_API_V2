package in.koreatech.koin.global.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import in.koreatech.koin.admin.abtest.useragent.UserAgentArgumentResolver;
import in.koreatech.koin.global.auth.AuthArgumentResolver;
import in.koreatech.koin.global.auth.ExtractAuthenticationInterceptor;
import in.koreatech.koin.global.auth.UserIdArgumentResolver;
import in.koreatech.koin.global.host.ServerURLArgumentResolver;
import in.koreatech.koin.global.host.ServerURLInterceptor;
import in.koreatech.koin.global.ipaddress.IpAddressArgumentResolver;
import in.koreatech.koin.global.ipaddress.IpAddressInterceptor;

@ExtendWith(MockitoExtension.class)
class WebConfigTest {

    private static final String DUPLICATE_JSON = "{\"name\":\"first\",\"name\":\"last\"}";
    private static final TypeReference<Map<String, String>> MAP_TYPE = new TypeReference<>() {
    };

    @Mock
    private ExtractAuthenticationInterceptor extractAuthenticationInterceptor;

    @Mock
    private IpAddressArgumentResolver ipAddressArgumentResolver;

    @Mock
    private ServerURLInterceptor serverURLInterceptor;

    @Mock
    private AuthArgumentResolver authArgumentResolver;

    @Mock
    private IpAddressInterceptor ipAddressInterceptor;

    @Mock
    private ServerURLArgumentResolver serverURLArgumentResolver;

    @Mock
    private UserIdArgumentResolver userIdArgumentResolver;

    @Mock
    private UserAgentArgumentResolver userAgentArgumentResolver;

    @Mock
    private CorsProperties corsProperties;

    @InjectMocks
    private WebConfig webConfig;

    @Test
    void replacesJacksonConvertersWithStrictCopiesWithoutMutatingSharedConverters() throws Exception {
        ObjectMapper firstMapper = mapperWithMarker("first-marker");
        ObjectMapper secondMapper = mapperWithMarker("second-marker");
        MappingJackson2HttpMessageConverter firstConverter =
            jacksonConverter(firstMapper, StandardCharsets.ISO_8859_1, "application/vnd.first+json");
        MappingJackson2HttpMessageConverter secondConverter =
            jacksonConverter(secondMapper, StandardCharsets.UTF_16, "application/vnd.second+json");

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(List.of(firstConverter, secondConverter));

        HttpMessageConverter<?> nonJacksonConverter = new StringHttpMessageConverter();
        HttpMessageConverter<?> anotherNonJacksonConverter = new StringHttpMessageConverter();
        List<HttpMessageConverter<?>> mvcConverters = new ArrayList<>();
        mvcConverters.add(nonJacksonConverter);
        mvcConverters.add(firstConverter);
        mvcConverters.add(anotherNonJacksonConverter);
        mvcConverters.add(secondConverter);

        webConfig.extendMessageConverters(mvcConverters);

        assertThat(mvcConverters).hasSize(4);
        assertThat(mvcConverters.get(0)).isSameAs(nonJacksonConverter);
        assertThat(mvcConverters.get(2)).isSameAs(anotherNonJacksonConverter);
        assertThat(mvcConverters.get(1)).isNotSameAs(firstConverter);
        assertThat(mvcConverters.get(3)).isNotSameAs(secondConverter);

        MappingJackson2HttpMessageConverter firstReplacement =
            (MappingJackson2HttpMessageConverter) mvcConverters.get(1);
        MappingJackson2HttpMessageConverter secondReplacement =
            (MappingJackson2HttpMessageConverter) mvcConverters.get(3);
        assertThat(firstReplacement.getObjectMapper()
            .isEnabled(StreamReadFeature.STRICT_DUPLICATE_DETECTION.mappedFeature())).isTrue();
        assertThat(secondReplacement.getObjectMapper()
            .isEnabled(StreamReadFeature.STRICT_DUPLICATE_DETECTION.mappedFeature())).isTrue();
        assertThat(firstReplacement.getObjectMapper()).isNotSameAs(firstMapper);
        assertThat(secondReplacement.getObjectMapper()).isNotSameAs(secondMapper);
        assertThat(firstReplacement.getObjectMapper().getRegisteredModuleIds()).contains("first-marker");
        assertThat(secondReplacement.getObjectMapper().getRegisteredModuleIds()).contains("second-marker");
        assertThat(firstReplacement.getSupportedMediaTypes())
            .containsExactlyElementsOf(firstConverter.getSupportedMediaTypes());
        assertThat(secondReplacement.getSupportedMediaTypes())
            .containsExactlyElementsOf(secondConverter.getSupportedMediaTypes());
        assertThat(firstReplacement.getDefaultCharset()).isEqualTo(StandardCharsets.ISO_8859_1);
        assertThat(secondReplacement.getDefaultCharset()).isEqualTo(StandardCharsets.UTF_16);

        assertThat(restTemplate.getMessageConverters()).containsExactly(firstConverter, secondConverter);
        assertThat(restTemplate.getMessageConverters().get(0)).isSameAs(firstConverter);
        assertThat(restTemplate.getMessageConverters().get(1)).isSameAs(secondConverter);
        assertThat(firstMapper.isEnabled(StreamReadFeature.STRICT_DUPLICATE_DETECTION.mappedFeature())).isFalse();
        assertThat(secondMapper.isEnabled(StreamReadFeature.STRICT_DUPLICATE_DETECTION.mappedFeature())).isFalse();
        assertThat(firstMapper.readValue(DUPLICATE_JSON, MAP_TYPE)).containsEntry("name", "last");
        assertThat(secondMapper.readValue(DUPLICATE_JSON, MAP_TYPE)).containsEntry("name", "last");
        assertThatThrownBy(() -> firstReplacement.getObjectMapper().readValue(DUPLICATE_JSON, MAP_TYPE))
            .isInstanceOf(JsonParseException.class);
        assertThatThrownBy(() -> secondReplacement.getObjectMapper().readValue(DUPLICATE_JSON, MAP_TYPE))
            .isInstanceOf(JsonParseException.class);
    }

    @Test
    void rejectsJacksonConverterSubclasses() {
        MappingJackson2HttpMessageConverter subclassConverter = new CustomJacksonHttpMessageConverter();
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(subclassConverter);

        assertThatThrownBy(() -> webConfig.extendMessageConverters(converters))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining(CustomJacksonHttpMessageConverter.class.getName());
    }

    private ObjectMapper mapperWithMarker(String markerName) {
        return new ObjectMapper().registerModule(new SimpleModule(markerName));
    }

    private MappingJackson2HttpMessageConverter jacksonConverter(
        ObjectMapper objectMapper,
        java.nio.charset.Charset defaultCharset,
        String customMediaType
    ) {
        MappingJackson2HttpMessageConverter converter =
            new MappingJackson2HttpMessageConverter(objectMapper);
        converter.setSupportedMediaTypes(List.of(MediaType.APPLICATION_JSON, MediaType.valueOf(customMediaType)));
        converter.setDefaultCharset(defaultCharset);
        return converter;
    }

    private static class CustomJacksonHttpMessageConverter extends MappingJackson2HttpMessageConverter {
    }
}
