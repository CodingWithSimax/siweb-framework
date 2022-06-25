package net.simax_dev.siweb.common.services;

public class NetworkClientInformation {
    private final String ip;
    private final String hostname;

    public NetworkClientInformation(String ip, String hostname) {
        this.ip = ip;
        this.hostname = hostname;
    }

    public String getIp() {
        return ip;
    }
    public String getHostname() {
        return hostname;
    }
}
