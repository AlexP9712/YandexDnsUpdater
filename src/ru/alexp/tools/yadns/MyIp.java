package ru.alexp.tools.yadns;

import ru.alexp.util.Net;

/**
 *
 * @author Александр
 */
public class MyIp {

    private static final String[] hosts = {"https://l2.io/ip", "http://www.telize.com/ip"};

    public static String getIp() {
        String ip;
        for (String host : hosts) {
            ip = Net.query(host).replaceAll("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})", "$1");
            if (ip != null) {
                return ip;
            }
        }
        return null;
    }
}
