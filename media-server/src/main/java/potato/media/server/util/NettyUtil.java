package potato.media.server.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.NetUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import potato.media.server.netty.handler.pull.HttpFileServerHandler;

import java.net.*;
import java.util.Enumeration;
import java.util.List;

/**
 * @author zh_zhou
 * created at 2020/02/07 23:00
 * Copyright [2020] [zh_zhou]
 */
public class NettyUtil {
    private static Logger logger = LoggerFactory.getLogger(NettyUtil.class);

    public static String getId(ChannelHandlerContext context) {
        return getId(context.channel());
    }

    public static String getId(Channel channel) {
        return channel.id().asLongText();
    }

    static String ip = null;


    public static String getIpAddress() {
        if(1>0){
            return NetUtils.getHostAddress();
        }
        if (StringUtils.isEmpty(ip)) {

            ip = getIpAddressInternal();
        }

        return ip;
    }

    private static String getIpAddressInternal() {
        try {
            Enumeration<NetworkInterface> er = NetworkInterface.getNetworkInterfaces();
            while (er.hasMoreElements()) {
                NetworkInterface ni = er.nextElement();
                if (ni.getName().startsWith("eth") || ni.getName().startsWith("bond")) {
                    List<InterfaceAddress> list = ni.getInterfaceAddresses();
                    for (InterfaceAddress interfaceAddress : list) {
                        InetAddress address = interfaceAddress.getAddress();
                        if (address instanceof Inet4Address) {
                            return address.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
        }

        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
        }

        return null;
    }

    public static String refineLocalIP(String ip) {
        if (StringUtils.isEmpty(ip))
            return ip;

        ip = ip.trim().toLowerCase();
        if (ServiceCommons.LOCALHOST_IP_V6.contains(ip))
            ip = ServiceCommons.LOCALHOST_IP;

        return ip;
    }

    public static String refineIPAddress(String ip) {
        if (StringUtils.isEmpty(ip)) {
            return null;
        }

        try {
            ip = ip.trim();
            ip = refineLocalIP(ip);

            InetAddress inetAddress = IPAddressUtil.parse(ip);
            if (inetAddress instanceof Inet6Address) {
                return IPAddressUtil.toIPv6SimpleAddress((Inet6Address) inetAddress);
            }
            return ip;
        } catch (Exception e) {
            logger.warn("refineIPAddress failed:" + ip, e);
            return ip;
        }
    }

}
