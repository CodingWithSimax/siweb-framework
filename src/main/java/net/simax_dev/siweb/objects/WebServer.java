package net.simax_dev.siweb.objects;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import net.simax_dev.siweb.Config;
import net.simax_dev.siweb.WebApplication;
import net.simax_dev.siweb.managers.WebServerLoader;
import net.simax_dev.siweb.managers.dependency_injection.DependencyManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Basic webserver for handling incoming requests
 */
public class WebServer {
    private static final Logger logger = LogManager.getLogger(WebServer.class);

    private final WebApplication webApplication;
    private final Config config;
    private final InetSocketAddress address;

    private final HttpServer httpServer;
    private final WebServerLoader webServerLoader;
    private final DependencyManager dependencyLoader;

    private final WebServerHttpHandler webServerHttpHandler;

    public WebServer(WebApplication webApplication, DependencyManager dependencyLoader, ClassLoader classLoader) throws IOException {
        this.webApplication = webApplication;
        this.dependencyLoader = dependencyLoader;
        this.webServerLoader = new WebServerLoader(webApplication, this, classLoader);
        this.config = webApplication.getConfig();
        this.address = this.config.getSocketAddress();
        this.httpServer = HttpServer.create(this.address, 0);

        this.webServerHttpHandler = new WebServerHttpHandler(webApplication, this);
        this.httpServer.createContext("/", this.webServerHttpHandler);
    }

    public void registerHandler(URIPath context, BiConsumer<HttpExchange, Map<String, String>> consumer) {
        this.webServerHttpHandler.registerHandler(context, consumer);
    }
    public void setFallback(BiConsumer<HttpExchange, Map<String, String>> consumer) {
        this.webServerHttpHandler.setFallbackHandler(consumer);
    }

    public void loadComponents() {
        try {
            this.webServerLoader.loadComponents(this.dependencyLoader.getComponents());
        } catch (IOException e) {
            throw new RuntimeException("Unable to load components", e);
        }
    }

    /**
     * Start the webserver; server will be started on the configured port in an external thread
     */
    public void start() {
        logger.debug("Starting webserver...");

        this.httpServer.start();

        logger.info("Webserver started on port " + this.address.getPort());
    }

    public void stop() {
        logger.debug("Stopping webserver...");

        this.httpServer.stop(0);

        logger.debug("Webserver stopped");
    }
}
