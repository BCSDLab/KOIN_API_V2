package in.koreatech.koin.batch.campus.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class YamlParser {

    private final Yaml yaml;

    public <T> T parse(String path, Class<T> clazz) throws IOException {
        String content = Files.readString(Paths.get(path));
        return yaml.loadAs(content, clazz);
    }

}
