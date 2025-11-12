package in.koreatech.koin.admin.bus.shuttle.util;

public class ExcelStringUtil {

    public static String extractDetailFromBrackets(String str) {
        int openIdx = str.indexOf('(');
        int closeIdx = str.indexOf(')');

        if (openIdx != -1 && closeIdx != -1 && closeIdx > openIdx) {
            return str.substring(openIdx + 1, closeIdx).trim();
        }

        return null;
    }

    public static String extractNameWithoutBrackets(String str) {
        int openIdx = str.indexOf('(');

        if (openIdx != -1) {
            return str.substring(0, openIdx).trim();
        }

        return str.trim();
    }
}
