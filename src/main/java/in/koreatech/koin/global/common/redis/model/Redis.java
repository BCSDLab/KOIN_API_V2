package in.koreatech.koin.global.common.redis.model;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;

@Component
public class Redis {

    public void setData(String key, Object data, Long time, TimeUnit timeUnit) throws IOException {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String value = data instanceof String ? (String)data : objectMapper.writeValueAsString(data);
        valOps.set(key, value, time, timeUnit);
    }
}
