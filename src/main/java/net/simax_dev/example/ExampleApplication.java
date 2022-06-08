package net.simax_dev.example;

import net.simax_dev.siweb.WebApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExampleApplication {
    private static final Logger logger = LogManager.getLogger(ExampleApplication.class.getName());

    public static void main(String[] args) {
        logger.info("Starting example application...");

        WebApplication webApplication = new WebApplication(ExampleApplication.class.getClassLoader());

        webApplication.loadDependencies(ExampleApplication.class.getPackage());

        webApplication.start();
    }
}
