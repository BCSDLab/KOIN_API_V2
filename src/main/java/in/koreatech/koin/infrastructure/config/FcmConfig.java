package in.koreatech.koin.infrastructure.config;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Profile("!local & !test")
public class FcmConfig {

    @PostConstruct
    public void initialize() {
        try {
            ClassPathResource resource = new ClassPathResource("koin-firebase-adminsdk.json");
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (FileNotFoundException e) {
            log.error("파일을 찾을 수 없습니다. ", e);
        } catch (IOException e) {
            log.error("FCM 인증이 실패했습니다. ", e);
        }
    }
}
