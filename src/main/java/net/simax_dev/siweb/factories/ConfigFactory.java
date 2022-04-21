package net.simax_dev.siweb.factories;

import net.simax_dev.siweb.Config;
import net.simax_dev.siweb.enums.CommunicationType;

import java.net.InetSocketAddress;

/**
 * Produces a config for the {@link net.simax_dev.siweb.WebApplication WebApplication}
 */
public class ConfigFactory {
    public static ConfigFactory create() {
        return new ConfigFactory();
    }

    // TODO implement
    public ConfigFactory insertConfig(String config) {
        return null;
    }


    private CommunicationType communicationType = CommunicationType.ACTIVE;

    /**
     * Set how the server does communicate with the webserver
     * @param communicationType can be passive, or active, for more look at {@link net.simax_dev.siweb.enums.CommunicationType CommunicationType}
     * @return configFactory
     */
    public ConfigFactory setCommunicationType(CommunicationType communicationType) {
        this.communicationType = communicationType;
        return this;
    }

    private InetSocketAddress socketAddress = new InetSocketAddress(4444);

    /**
     * The address the server will be listening to.
     * By default, the value is set to port 4444
     * @param socketAddress the socket address the server will be listening to
     * @return configFactory
     */
    public ConfigFactory setSocketAddress(InetSocketAddress socketAddress) {
        this.socketAddress = socketAddress;
        return this;
    }

    public Config build() {
        return Config._create(
                this.communicationType,
                this.socketAddress
        );
    }
}
