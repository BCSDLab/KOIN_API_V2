package in.koreatech.koin.global.common.email.model;

import java.util.Hashtable;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import in.koreatech.koin.global.common.email.exception.InvalidEmailDomainException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Domain {

    private final String value;

    public static Domain from(String fullAddress) {
        return new Domain(domainFrom(fullAddress));
    }

    private static String domainFrom(String fullAddress) {
        return fullAddress.substring(
            Email.getSeparateIndex(fullAddress) + Email.domainSeparator.length());
    }

    boolean validate() {
        try {
            Hashtable<String, String> environmentSetting = new Hashtable<>();

            environmentSetting.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            //DNS Lookup By MX
            //https://www.rgagnon.com/javadetails/java-0452.html
            Attribute attribute = ((DirContext)new InitialDirContext(environmentSetting)).getAttributes(value,
                new String[] {"MX"}).get("MX");

            return attribute != null;

        } catch (NamingException exception) {
            throw InvalidEmailDomainException.withDetail("emailDomain: " + value);
        }
    }
}
