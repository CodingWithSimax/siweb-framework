package net.simax_dev.siweb;

import net.simax_dev.WebServer;
import net.simax_dev.siweb.factories.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebApplication {
    private static final Logger logger = LogManager.getLogger(WebApplication.class);

    private final Config config;
    private final WebServer webServer;

    public WebApplication() {
        this(ConfigFactory.create().build());
    }
    public WebApplication(Config config) {
        this.config = config;
        this.webServer = new WebServer();
    }
}
