package in.koreatech.koin.support;

import java.util.Map;

import org.assertj.core.api.Assertions;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonAssertions {


    public static JsonStringAssert assertThat(String expect) {
        return new JsonStringAssert(expect);
    }

    public static class JsonStringAssert {

        private final ObjectMapper objectMapper = new ObjectMapper();
        private final String expect;

        JsonStringAssert(String expect) {
            this.expect = expect;
        }

        public void isEqualTo(String actual) {
            try {
                Map<String, Object> responseMap = objectMapper.readValue(expect, new TypeReference<>() {
                });
                Map<String, Object> expectedMap = objectMapper.readValue(actual, new TypeReference<>() {
                });
                Assertions.assertThat(responseMap).isEqualTo(expectedMap);
            } catch (Exception ignored) {

            }
        }

    }
}
