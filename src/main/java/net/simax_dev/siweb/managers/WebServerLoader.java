package net.simax_dev.siweb.managers;

import net.simax_dev.siweb.WebApplication;
import net.simax_dev.siweb.annotations.Component;
import net.simax_dev.siweb.annotations.Page;
import net.simax_dev.siweb.loader.TemplateLoader;
import net.simax_dev.siweb.objects.WebServer;

import java.io.IOException;
import java.util.Set;

public class WebServerLoader {
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
        for (Class<?> component : components) {
            if (component.isAnnotationPresent(Page.class)) {
                this.loadComponentPage(component);
            }
        }
    }

    private void loadComponentPage(Class<?> component) {
        Page page = component.getAnnotation(Page.class);
        Component componentAnnotation = component.getAnnotation(Component.class);

        String webURL = page.value();
        String templateURL = componentAnnotation.templateURL();
        String styleURL = componentAnnotation.styleURL();

        try {
            this.templateLoader.load(templateURL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
