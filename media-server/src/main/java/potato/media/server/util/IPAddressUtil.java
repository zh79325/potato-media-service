package potato.media.server.util;

import com.google.common.net.InetAddresses;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

/**
 * @author zh_zhou
 * created at 2020/02/07 23:09
 * Copyright [2020] [zh_zhou]
 */
public class IPAddressUtil {
    private static final int IPV6_FULL_LENGTH = 39;
    private static final char[] hexArray = "0123456789abcdef".toCharArray();

    public IPAddressUtil() {
    }

    public static InetAddress parse(String input) {
        return InetAddresses.forString(input);
    }

    public static boolean isIPAddress(String input) {
        try {
            parse(input);
            return true;
        } catch (IllegalArgumentException var2) {
            return false;
        }
    }

    public static boolean isIPv4Address(String input) {
        try {
            InetAddress address = parse(input);
            return address instanceof Inet4Address;
        } catch (IllegalArgumentException var2) {
            return false;
        }
    }

    public static boolean isIPv6Address(String input) {
        try {
            InetAddress address = parse(input);
            return address instanceof Inet6Address;
        } catch (IllegalArgumentException var2) {
            return false;
        }
    }

    public static String toIPv6SimpleAddress(String input) {
        InetAddress address = parse(input);
        boolean isIPv6Address = address instanceof Inet6Address;
        if (!isIPv6Address) {
            throw new IllegalArgumentException(String.format("'%s' is not an IPv6 string literal.", input));
        } else {
            return toIPv6SimpleAddress((Inet6Address)address);
        }
    }

    public static String toIPv6SimpleAddress(Inet6Address ipv6) {
        return InetAddresses.toAddrString(ipv6);
    }

    public static String toIPv6FullAddress(String input) {
        InetAddress address = parse(input);
        boolean isIPv6Address = address instanceof Inet6Address;
        if (!isIPv6Address) {
            throw new IllegalArgumentException(String.format("'%s' is not an IPv6 string literal.", input));
        } else {
            return toIPv6FullAddress((Inet6Address)address);
        }
    }

    public static String toIPv6FullAddress(Inet6Address ipv6) {
        byte[] bytes = ipv6.getAddress();
        char[] hexChars = new char[40];
        int i = 0;

        for(int j = 0; i < bytes.length; j += 5) {
            int x = bytes[i + 0] & 255;
            int y = bytes[i + 1] & 255;
            hexChars[j + 0] = hexArray[x >>> 4];
            hexChars[j + 1] = hexArray[x & 15];
            hexChars[j + 2] = hexArray[y >>> 4];
            hexChars[j + 3] = hexArray[y & 15];
            hexChars[j + 4] = ':';
            i += 2;
        }

        return new String(hexChars, 0, 39);
    }
}
