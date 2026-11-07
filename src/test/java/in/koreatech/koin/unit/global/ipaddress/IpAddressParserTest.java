package in.koreatech.koin.unit.global.ipaddress;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import in.koreatech.koin.global.ipaddress.IpAddressParser;

class IpAddressParserTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "0.0.0.0", "127.0.0.1", "192.0.2.1", "255.255.255.255",
        "::", "::1", "2001:DB8::a", "2001:db8:1234:5678:9abc:def0:1234:5678",
        "::ffff:192.0.2.1", "1:2:3:4:5:6:255.255.255.255"
    })
    void 유효한_단일_IP의_표기를_유지한다(String address) {
        assertThat(IpAddressParser.parse(address)).contains(address);
    }

    @Test
    void 전체_길이가_45자를_넘어도_첫_주소가_정상이면_추출한다() {
        String value = "2001:db8:1234:5678:9abc:def0:1234:5678, 10.0.0.12";

        assertThat(value).hasSizeGreaterThan(45);
        assertThat(IpAddressParser.parse(value)).contains("2001:db8:1234:5678:9abc:def0:1234:5678");
    }

    @Test
    void 첫_주소의_앞뒤_공백을_제거한다() {
        assertThat(IpAddressParser.parse(" \t203.0.113.10 \t, 10.0.0.1")).contains("203.0.113.10");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        " ", "unknown", "localhost", "example.com", "0x7f000001", "2130706433", "127.1",
        "256.0.0.1", "1.2.3.4.5", "1.2.3", "1.2.3.", "1.2..4", "1.2.3.-1", "1.2.3.+1",
        "01.2.3.4", "1.2.3.01", "1.2.3.000", "192.0.2.1:443", "[::1]", "[::1]:443", "fe80::1%eth0",
        "fe80::1%1", "2001:db8:::1", "2001:db8:1:2:3:4:5:6:7", "2001:db8::gggg", "2001:db8::example.com",
        "::ffff:192.0.2.001", "::ffff:192.0.2.256", ".::1", ".::192.0.2.1",
        ", 192.0.2.1", "unknown, 192.0.2.1"
    })
    void 잘못된_첫_주소는_거부한다(String value) {
        assertThat(IpAddressParser.parse(value)).isEmpty();
    }

    @Test
    void 첫_주소_자체가_45자를_넘으면_거부한다() {
        assertThat(IpAddressParser.parse("a".repeat(46) + ", 192.0.2.1")).isEmpty();
    }
}
