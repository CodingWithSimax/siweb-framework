package net.simax_dev.siweb;

import net.simax_dev.siweb.enums.CommunicationType;

import java.net.InetSocketAddress;

/**
 * The Config needed for creation of the webserver
 * will be automatically loaded from resources/siweb.json; or can be set manually
 */
public class Config {
    public static Config _create(
            CommunicationType communicationType,
            InetSocketAddress socketAddress,
            String indexHTML
    ) {
        return new Config(
                communicationType,
                socketAddress,
                indexHTML
        );
    }

    private Config(
            CommunicationType communicationType,
            InetSocketAddress socketAddress,
            String indexHTML
    ) {
        this.communicationType = communicationType;
        this.socketAddress = socketAddress;
        this.indexHTML = indexHTML;
    }

    private final CommunicationType communicationType;

    public CommunicationType getCommunicationType() {
        return this.communicationType;
    }

    private final InetSocketAddress socketAddress;

    public InetSocketAddress getSocketAddress() {
        return this.socketAddress;
    }

    private final String indexHTML;

    public String getIndexHTML() {
        return this.indexHTML;
    }
}
