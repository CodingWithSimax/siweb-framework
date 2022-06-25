package net.simax_dev.siweb.managers;

import com.sun.net.httpserver.HttpExchange;
import net.simax_dev.siweb.WebApplication;
import net.simax_dev.siweb.annotations.Component;
import net.simax_dev.siweb.annotations.Page;
import net.simax_dev.siweb.loader.TemplateGenerator;
import net.simax_dev.siweb.loader.TemplateLoader;
import net.simax_dev.siweb.objects.URIPath;
import net.simax_dev.siweb.objects.WebServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class WebServerLoader {
    private static final Logger logger = LogManager.getLogger(WebServerLoader.class);

    private final WebApplication webApplication;
    private final WebServer webServer;
    private final TemplateLoader templateLoader;
    private final TemplateGenerator templateGenerator;
    private final ClassLoader classLoader;

    private String headHTML;

    public WebServerLoader(WebApplication webApplication, WebServer webServer, ClassLoader classLoader) {
        this.webApplication = webApplication;
        this.webServer = webServer;
        this.classLoader = classLoader;
        this.templateLoader = new TemplateLoader(classLoader);
        this.templateGenerator = new TemplateGenerator();
    }

    public void loadComponents(Set<Class<?>> components) throws IOException {
        System.out.println("loading components...");

        System.out.println("index html: " + this.webApplication.getConfig().getIndexHTML());
        InputStream inputStream = this.classLoader.getResourceAsStream(this.webApplication.getConfig().getIndexHTML());
        assert inputStream != null;
        this.headHTML = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        inputStream.close();

        components.forEach(this::loadComponentPage);
    }

    private void loadComponentPage(Class<?> component) {
        logger.debug("loading component " + component.getSimpleName());

        Component componentAnnotation = component.getAnnotation(Component.class);

        //String webURL = page.value();
        String templateURL = componentAnnotation.templateURL();
        String styleURL = componentAnnotation.styleURL();

        if (styleURL.equals("")) styleURL = null;

        TemplateLoader.TemplateData templateData;
        try {
            templateData = this.templateLoader.load(templateURL, styleURL);
        } catch (IOException e) {
            throw new RuntimeException("error while loading template '" + templateURL + "'", e);
        }

        if (component.isAnnotationPresent(Page.class)) {
            Page page = component.getAnnotation(Page.class);

            if (page.isFallback()) {
                this.webServer.setFallback(this.getConsumer(templateData));
            }
            if (!page.value().isEmpty()) {
                this.webServer.registerHandler(URIPath.of(page.value()), this.getConsumer(templateData));
            }
        }
    }

    private BiConsumer<HttpExchange, Map<String, String>> getConsumer(TemplateLoader.TemplateData templateData) {
        String resultHTML = this.templateGenerator.generateTemplate(this.headHTML, templateData.getHTML());
        return ((exchange, stringStringMap) -> {
            try {
                exchange.getResponseHeaders().set("Content-Type", "text/html");
                exchange.sendResponseHeaders(200, resultHTML.length());

                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(resultHTML.getBytes(StandardCharsets.UTF_8));
                outputStream.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
    }
}
