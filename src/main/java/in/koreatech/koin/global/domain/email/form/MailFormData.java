package in.koreatech.koin.global.domain.email.form;

import java.util.Map;

public interface MailFormData {

    Map<String, String> getContent();

    String getSubject();

    String getFilePath();
}
