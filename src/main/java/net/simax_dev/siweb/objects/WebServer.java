package net.simax_dev.siweb.objects;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.simax_dev.siweb.Config;
import net.simax_dev.siweb.WebApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Basic webserver for handling incoming requests
 */
public class WebServer {
    private static final Logger logger = LogManager.getLogger(WebServer.class);

    private final WebApplication webApplication;
    private final Config config;
    private final InetSocketAddress address;

    private final HttpServer httpServer;

    public WebServer(WebApplication webApplication) throws IOException {
        this.webApplication = webApplication;
        this.config = webApplication.getConfig();
        this.address = this.config.getSocketAddress();
        this.httpServer = HttpServer.create(this.address, 0);
    }

    public void registerHandler(String context, HttpHandler handler) {
        this.httpServer.createContext(context, handler);
    }

    /**
     * Start the webserver; server will be started on the configured port in an external thread
     */
    public void start() {
        logger.debug("Starting webserver...");

        this.httpServer.start();

        logger.info("Webserver started on port " + this.address.getPort());
    }
}
