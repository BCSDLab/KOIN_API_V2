package in.koreatech.koin.acceptance.support;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.hibernate.query.sqm.sql.ConversionException;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import in.koreatech.koin.domain.bus.dto.SingleBusTimeResponse;

public class JsonAssertions {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.findAndRegisterModules(); // 다른 모듈들도 자동 등록
    }

    public static JsonStringAssert assertThat(String expect) {
        return new JsonStringAssert(expect);
    }

    public static JsonNode convertJsonNode(MvcResult mvcResult) {
        try {
            return objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        } catch (JsonProcessingException e) {
            throw new ConversionException("JsonString to JsonNode convert exception: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw new ConversionException("Response to String convert exception: " + e.getMessage());
        }
    }

    public static <T> List<T> convertToList(JsonNode jsonNode, Class<T> clazz) {
        try {
            return objectMapper.readValue(
                jsonNode.toString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, clazz)
            );
        } catch (JsonProcessingException e) {
            throw new ConversionException("JsonNode to List conversion exception: " + e.getMessage());
        }
    }

    public static class JsonStringAssert {

        private final String expect;

        JsonStringAssert(String expect) {
            this.expect = expect;
        }

        public void isEqualTo(String actual) {
            try {
                Object responseObj = parseJson(expect);
                Object expectedObj = parseJson(actual);

                Assertions.assertThat(responseObj).isEqualTo(expectedObj);
            } catch (Exception e) {
                throw new AssertionError("json parsing error\n" + e.getMessage());
            }
        }

        private Object parseJson(String json) throws IOException {
            try {
                return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
                });
            } catch (IOException e) {
                return objectMapper.readValue(json, new TypeReference<List<Object>>() {
                });
            }
        }
    }
}
