package in.koreatech.koin.admin.bus.shuttle.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NameParser {

    private static final Pattern PATTERN = Pattern.compile("^(.*?)\\s*(?:\\((.*?)\\))?$");

    public static ParsedName parse(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return new ParsedName("", null);
        }

        Matcher m = PATTERN.matcher(raw.trim());

        if (m.matches()) {
            String name = m.group(1).trim();
            String detail = m.group(2) != null ? m.group(2).trim() : null;
            return new ParsedName(name, detail);
        }

        return new ParsedName(raw.trim(), null);
    }

    public record ParsedName(String name, String detail) {}
}
