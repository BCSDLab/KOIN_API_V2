package in.koreatech.koin.global.ipaddress;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.regex.Pattern;

public final class IpAddressParser {

    private static final int MAX_IP_LENGTH = 45;
    private static final Pattern IPV6_CHARACTERS = Pattern.compile("[0-9a-fA-F:][0-9a-fA-F:.]*");

    private IpAddressParser() {
    }

    // 첫 주소의 형식만 검사하며, 전달한 프록시의 신뢰 여부는 검증하지 않는다.
    public static Optional<String> parse(String value) {
        if (value == null) {
            return Optional.empty();
        }

        int separator = value.indexOf(',');
        String address = (separator < 0 ? value : value.substring(0, separator)).trim();
        if (address.isEmpty() || address.length() > MAX_IP_LENGTH) {
            return Optional.empty();
        }

        return isLiteralAddress(address) ? Optional.of(address) : Optional.empty();
    }

    private static boolean isLiteralAddress(String address) {
        if (address.indexOf(':') < 0) {
            return isIpv4(address);
        }
        if (!IPV6_CHARACTERS.matcher(address).matches()) {
            return false;
        }
        if (address.indexOf('.') >= 0 && !isIpv4(address.substring(address.lastIndexOf(':') + 1))) {
            return false;
        }

        try {
            // ':'를 포함한 숫자 IPv6 표기만 전달하므로 호스트 이름을 DNS로 조회하지 않는다.
            InetAddress.getByName(address);
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    private static boolean isIpv4(String address) {
        String[] octets = address.split("\\.", -1);
        if (octets.length != 4) {
            return false;
        }

        for (String octet : octets) {
            if (octet.isEmpty() || octet.length() > 3 || (octet.length() > 1 && octet.charAt(0) == '0')) {
                return false;
            }
            int value = 0;
            for (int i = 0; i < octet.length(); i++) {
                char digit = octet.charAt(i);
                if (digit < '0' || digit > '9') {
                    return false;
                }
                value = value * 10 + digit - '0';
            }
            if (value > 255) {
                return false;
            }
        }
        return true;
    }
}
