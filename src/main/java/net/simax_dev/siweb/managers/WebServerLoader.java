package net.simax_dev.siweb.managers;

import net.simax_dev.siweb.WebApplication;
import net.simax_dev.siweb.annotations.Component;
import net.simax_dev.siweb.annotations.Page;
import net.simax_dev.siweb.loader.TemplateLoader;
import net.simax_dev.siweb.objects.WebServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Set;

public class WebServerLoader {
    private static final Logger logger = LogManager.getLogger(WebServerLoader.class);

    private final WebApplication webApplication;
    private final WebServer webServer;
    private final TemplateLoader templateLoader;

    public WebServerLoader(WebApplication webApplication, WebServer webServer, ClassLoader classLoader) {
        this.webApplication = webApplication;
        this.webServer = webServer;
        this.templateLoader = new TemplateLoader(classLoader);
    }

    public void loadComponents(Set<Class<?>> components) {
        System.out.println("loading components...");
        components.forEach(this::loadComponentPage);
    }

    private void loadComponentPage(Class<?> component) {
        logger.debug("loading component " + component.getSimpleName());

        Component componentAnnotation = component.getAnnotation(Component.class);

        //String webURL = page.value();
        String templateURL = componentAnnotation.templateURL();
        String styleURL = componentAnnotation.styleURL();

        if (styleURL.equals("")) styleURL = null;

        try {
            this.templateLoader.load(templateURL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
