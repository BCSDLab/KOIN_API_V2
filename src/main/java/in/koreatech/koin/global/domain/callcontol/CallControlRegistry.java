package in.koreatech.koin.global.domain.callcontol;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class CallControlRegistry {

    private final Map<String, CallControl> callControlMaps = new HashMap<>();

    public void registerCallControl(String name, CallControl callControl) {
        callControlMaps.put(name, callControl);
    }

    public CallControl getCallControl(String name) {
        return callControlMaps.get(name);
    }
}
