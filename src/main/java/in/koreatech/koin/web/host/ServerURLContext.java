package in.koreatech.koin.web.host;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class ServerURLContext {

    private String serverURL;

    public String getServerURL() {
        return serverURL;
    }

    public void setServerURL(String host) {
        this.serverURL = host;
    }
}
