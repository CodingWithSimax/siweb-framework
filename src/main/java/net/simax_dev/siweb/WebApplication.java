package net.simax_dev.siweb;

import net.simax_dev.siweb.factories.ConfigFactory;
import net.simax_dev.siweb.managers.dependency_injection.DependencyLoader;
import net.simax_dev.siweb.objects.WebServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class WebApplication {
    private static final Logger logger = LogManager.getLogger(WebApplication.class);

    private final Config config;
    private final WebServer webServer;
    private final DependencyLoader dependencyLoader;

    public WebApplication() {
        this(ConfigFactory.create().build());
    }
    public WebApplication(Config config) {
        this.config = config;
        try {
            this.webServer = new WebServer(this);
        } catch (IOException e) {
            logger.error("Could not create webserver", e);
            throw new RuntimeException(e);
        }
        this.dependencyLoader = new DependencyLoader(this);
    }

    public Config getConfig() {
        return this.config;
    }

    public void loadDependencies(Class<?> ...classes) {
        this.dependencyLoader.loadDependencies(classes);
    }
    public void loadDependencies(Package pkg) {
        this.dependencyLoader.loadDependencies(pkg);
    }
    public void loadDependency(Class<?> clazz) {
        this.dependencyLoader.loadDependency(clazz);
    }

    /**
     * Start the web application, including webserver
     */
    public void start() {
        logger.debug("Starting Web Application...");

        // initialise dependencies
        this.dependencyLoader.load();

        // start webserver
        this.webServer.start();
    }
}
