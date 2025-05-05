package in.koreatech.koin._common.model;

import java.util.Map;

public interface MailFormData {

    Map<String, String> getContent();

    String getSubject();

    String getFilePath();
}
