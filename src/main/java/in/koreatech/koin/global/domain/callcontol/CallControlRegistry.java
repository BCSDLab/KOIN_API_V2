package in.koreatech.koin.global.domain.callcontol;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class CallControlRegistry {

    private final Map<String, CallControl> callControlMaps = new HashMap<>();

    public CallControl callControl(String name, CallControl callControl) {
        callControlMaps.put(name, callControl);
        return callControl;
    }
}
