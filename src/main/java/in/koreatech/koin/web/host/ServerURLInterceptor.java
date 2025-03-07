package in.koreatech.koin.web.host;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ServerURLInterceptor implements HandlerInterceptor {

    private final ServerURLContext serverURLContext;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String serverURL = getServerURL(request);
        serverURLContext.setServerURL(serverURL);
        return true;
    }

    public String getServerURL(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        return (serverPort != 80 && serverPort != 443) ?
            String.format("%s://%s:%d", scheme, serverName, serverPort) :
            String.format("%s://%s", scheme, serverName);
    }
}
