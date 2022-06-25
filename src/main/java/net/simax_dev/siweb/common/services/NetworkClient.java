package net.simax_dev.siweb.common.services;

import net.simax_dev.siweb.annotations.InternService;

@InternService
public class NetworkClient {
    public NetworkClient(
            NetworkClientInformation networkClientInformation
    ) {
        System.out.println("init user data! Got ip: " + networkClientInformation.getHostname());
    }

    public void test() {
        System.out.println("Hallo");
    }
}
