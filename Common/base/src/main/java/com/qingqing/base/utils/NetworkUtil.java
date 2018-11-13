package com.qingqing.base.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * 网络相关工具类
 */
public final class NetworkUtil {

    public enum ConnectionType {
        NONE, UNKNOWN, ETHERNET, WIFI, G_UNKNOWN, G2, G3, G4
    }

    /**
     * Returns whether the network is available
     *
     * @return 网络是否可用
     * @see [类、类#方法、类#成员]
     */
    public static boolean isNetworkAvailable() {
        return getConnectedNetworkInfo() != null;
    }

    /**
     * 获取网络类型
     *
     * @return 网络类型
     * @see [类、类#方法、类#成员]
     */
    public static int getNetworkType() {
        NetworkInfo networkInfo = getConnectedNetworkInfo();
        if (networkInfo != null) {
            return networkInfo.getType();
        }

        return -1;
    }

    public static NetworkInfo getConnectedNetworkInfo() {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) UtilsMgr.getCtx()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity == null) {
                Log.w("network", "couldn't get connectivity manager");
            } else {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                if (info != null) {
                    for (NetworkInfo anInfo : info) {
                        if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                            return anInfo;
                        }
                    }
                }
            }

        } catch (Exception e) {
            Log.w("network", e.toString(), e);
        }
        return null;
    }

    public static boolean isMobileConnected() {
        return (ConnectivityManager.TYPE_MOBILE == getNetworkType());
    }

    public static boolean isWifiConnected() {
        return (ConnectivityManager.TYPE_WIFI == getNetworkType());
    }

    public static boolean isEthernetConnected() {
        return (ConnectivityManager.TYPE_ETHERNET == getNetworkType());
    }

    public static boolean isWifiOrEthernetConnected() {
        return isWifiConnected() || isEthernetConnected();
    }

    /**
     * 获取网络连接的字串描述
     */
    public static String getConnectionTypeString() {
        ConnectionType type = getConnectionType();
        switch (type) {
            case UNKNOWN:
            default:
                return "unknown";
            case ETHERNET:
                return "ethernet";
            case WIFI:
                return "wifi";
            case G_UNKNOWN:
                return "g_unknown";
            case G2:
                return "2g";
            case G3:
                return "3g";
            case G4:
                return "4g";
        }
    }

    public static ConnectionType getConnectionType() {

        if (!isNetworkAvailable())
            return ConnectionType.NONE;

        if (isWifiConnected()) {
            return ConnectionType.WIFI;
        } else {
            NetworkInfo networkInfo = getConnectedNetworkInfo();
            if (networkInfo == null) {
                return ConnectionType.UNKNOWN;
            }
            int nType = networkInfo.getType();

            switch (nType) {
                case ConnectivityManager.TYPE_WIFI:
                    return ConnectionType.WIFI;
                case ConnectivityManager.TYPE_ETHERNET:
                    return ConnectionType.ETHERNET;
                case ConnectivityManager.TYPE_MOBILE:
                    int subType = networkInfo.getSubtype();
                    switch (subType) {
                        case TelephonyManager.NETWORK_TYPE_GPRS:
                        case TelephonyManager.NETWORK_TYPE_EDGE:
                        case TelephonyManager.NETWORK_TYPE_CDMA:
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN:
                            return ConnectionType.G2;
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        case TelephonyManager.NETWORK_TYPE_EHRPD:
                        case TelephonyManager.NETWORK_TYPE_HSPAP:
                            return ConnectionType.G3;
                        case TelephonyManager.NETWORK_TYPE_LTE:
                            return ConnectionType.G4;
                        default:
                            // 三种3G制式
                            String subTypeName = networkInfo.getSubtypeName();
                            if (subTypeName.equalsIgnoreCase("TD-SCDMA")
                                    || subTypeName.equalsIgnoreCase("WCDMA")
                                    || subTypeName.equalsIgnoreCase("CDMA2000")) {
                                return ConnectionType.G3;
                            } else {
                                return ConnectionType.UNKNOWN;
                            }
                    }
                default:
                    return ConnectionType.UNKNOWN;
            }
        }
    }

    /**
     * 获取wifi名称
     */
    public static String getWifiSSID() {
        if (isWifiConnected()) {
            try {
                WifiManager wifi = (WifiManager) UtilsMgr.getCtx().getApplicationContext()
                        .getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = wifi.getConnectionInfo();
                return info.getSSID();
            } catch (Exception e) {
                Log.w("network", e.toString(), e);
            }
        }
        return null;
    }

    /**
     * Returns whether the network is roaming
     *
     * @return boolean
     * @see [类、类#方法、类#成员]
     */
    public static boolean isMobileRoaming() {

        if (isMobileConnected())
            return false;

        TelephonyManager telephonyManager = (TelephonyManager) UtilsMgr.getCtx()
                .getSystemService(Context.TELEPHONY_SERVICE);

        return telephonyManager.isNetworkRoaming();
    }

    /**
     * 获取本机ip。
     */
    public static String getLocalIpAddress() {
        // String ipaddress = "";
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface intf = en.nextElement();
                Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
                while (enumIpAddr.hasMoreElements()) {
                    InetAddress inetAddress = enumIpAddr.nextElement();

                    // loopback地址就是代表本机的IP地址，只要第一个字节是127，就是lookback地址
                    if (!inetAddress.isLoopbackAddress()
                            && !inetAddress.isLinkLocalAddress()) {
                        return inetAddress.getHostAddress();
                        // ipaddress = ipaddress + ";" +
                        // inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception e) {
            Log.w("network", e.toString(), e);
        }
        return null;
        // return ipaddress;
    }

    /**
     * 判断端口是否被占用
     */
    public static boolean isPortUsed(int port) {
        String[] cmds = {"netstat", "-an"};
        Process process = null;
        InputStream is = null;
        BufferedReader dis = null;
        boolean isUsing = false;
        try {

            String line = "";
            Runtime runtime = Runtime.getRuntime();

            process = runtime.exec(cmds);
            is = process.getInputStream();
            dis = new BufferedReader(new InputStreamReader(is));
            while ((line = dis.readLine()) != null) {
                // Log.w("network",line);
                if (line.contains(":" + port)) {
                    isUsing = true;
                    break;
                }
            }
        } catch (Exception e) {
            Log.w("network", e.toString(), e);
        } finally {
            try {
                if (dis != null) {
                    dis.close();
                }
                if (is != null) {
                    is.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                Log.w("network", e.toString(), e);
            }
        }
        return isUsing;
    }

    public static String getMacAddressPure() {
        String addr = getMacAddress();
        if (addr == null) {
            return "";
        }
        String addrNoColon = addr.replaceAll(":", "");
        String addrNoLine = addrNoColon.replaceAll("-", "");

        return StringUtil.toUpperCase(addrNoLine);

    }

    private static String getWifiMacAddress() {
        try {
            WifiManager wifi = (WifiManager) UtilsMgr.getCtx().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifi.getConnectionInfo();
            return info.getMacAddress();
        } catch (Exception e) {
            Log.w("network", e.toString(), e);
        }
        return "";
    }

    public static String getMacAddress() {
        String addr = getMacAddrInFile("/sys/class/net/eth0/address");
        if (TextUtils.isEmpty(addr)) {
            addr = getMacAddrInFile("/sys/class/net/wlan0/address");
        }

        if (TextUtils.isEmpty(addr)) {
            return getWifiMacAddress();
        }

        return addr;
    }

    private static String getMacAddrInFile(String filepath) {
        File f = new File(filepath);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            BufferedReader rd = new BufferedReader(new InputStreamReader(fis));
            String str = rd.readLine();
            if (TextUtils.isEmpty(str))
                return "";
            // 去除空格
            str = str.replaceAll(" ", "");

            // 查看是否是全0的无效MAC地址 如 00:00:00:00:00:00
            String p = str.replaceAll("-", "");
            p = p.replaceAll(":", "");
            if (p.matches("0*")) {
                return null;
            }
            return str;
        } catch (Exception e) {
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    /**
     * Load UTF8withBOM or any ansi text file.
     *
     * @param filename
     * @return
     * @throws IOException
     */
    public static String loadFileAsString(String filename) throws IOException {
        final int BUFLEN = 1024;
        BufferedInputStream is = new BufferedInputStream(new FileInputStream(filename),
                BUFLEN);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFLEN);
            byte[] bytes = new byte[BUFLEN];
            boolean isUTF8 = false;
            int read, count = 0;
            while ((read = is.read(bytes)) != -1) {
                if (count == 0 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB
                        && bytes[2] == (byte) 0xBF) {
                    isUTF8 = true;
                    baos.write(bytes, 3, read - 3); // drop UTF8 bom marker
                } else {
                    baos.write(bytes, 0, read);
                }
                count += read;
            }
            return isUTF8 ? new String(baos.toByteArray(), "UTF-8")
                    : new String(baos.toByteArray());
        } finally {
            try {
                is.close();
            } catch (Exception ex) {
            }
        }
    }
}