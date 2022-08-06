package xyz.idaoteng.auth.utils;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@Slf4j
public class NetUtil {
    private static boolean hasIp(String ip) {
        return ip != null && !"".equals(ip) && !"unknown".equalsIgnoreCase(ip);
    }

    private static String getFirstIp(String ip) {
        // 对于通过多个代理的情况，第一个IP为客户端真实IP，多个IP按照','分割
        int index = ip.indexOf(',');
        if (index != -1) {
            //只获取第一个值
            String realIp = ip.substring(0, index);
            log.debug("realIp = {}", realIp);
            return realIp;
        } else {
            log.debug("realIp = {}", ip);
            return ip;
        }
    }

    public static String tryToGetRealIp(HttpServletRequest request) {
        String ip;

        //检查请求是否被各种web服务器代理

        ip = request.getHeader("X-Real-IP");
        if (hasIp(ip)) {
            return getFirstIp(ip);
        }

        ip = request.getHeader("Proxy-Client-IP");
        if (hasIp(ip)) {
            return getFirstIp(ip);
        }

        ip  =  request.getHeader("WL-Proxy-Client-IP");
        if (hasIp(ip)) {
            return getFirstIp(ip);
        }

        ip = request.getHeader("X-Forwarded-For");
        if (hasIp(ip)) {
            return getFirstIp(ip);
        }

        ip = request.getRemoteAddr();
        List<String> ipList = getAllLocalHostIp();
        if (!ipList.contains(ip)) {
            return ip;
        }

        log.debug("客户端IP为本地IP: {}", ip);
        return null;
    }

    //获取所有的本地地址
    public static List<String> getAllLocalHostIp() {
        List<String> ipList = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            NetworkInterface currentNetworkInterface;
            InetAddress currentInetAddress;
            String ip;
            while (networkInterfaces.hasMoreElements()) {
                currentNetworkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = currentNetworkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    currentInetAddress = inetAddresses.nextElement();
                    if (currentInetAddress != null ) { // && currentInetAddress instanceof Inet4Address 仅获取IPV4
                        ip = currentInetAddress.getHostAddress();
                        ipList.add(ip);
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ipList;
    }

    //获取本机Mac地址
    public static byte[] getMacAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            NetworkInterface currentNetworkInterface;
            if (networkInterfaces != null) {
                while (networkInterfaces.hasMoreElements()) {
                    currentNetworkInterface = networkInterfaces.nextElement();
                    if (!currentNetworkInterface.isLoopback()) {
                        byte[] macAddress = currentNetworkInterface.getHardwareAddress();
                        if (macAddress != null) {
                            log.debug("mac地址 = {}", macAddress);
                            return macAddress;
                        }
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
