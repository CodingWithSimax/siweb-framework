package net.simax_dev.siweb.objects;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.simax_dev.siweb.WebApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class WebServerHttpHandler implements HttpHandler {
    private static final Logger logger = LogManager.getLogger(WebServerHttpHandler.class.getName());

    private final WebApplication webApplication;
    private final WebServer webServer;

    private final Map<URIPath, BiConsumer<HttpExchange, Map<String, String>>> handlers;
    private BiConsumer<HttpExchange, Map<String, String>> fallback = null;


    public WebServerHttpHandler(WebApplication webApplication, WebServer webServer) {
        this.webApplication = webApplication;
        this.webServer = webServer;

        this.handlers = new HashMap<>();
    }

    public void registerHandler(URIPath path, BiConsumer<HttpExchange, Map<String, String>> consumer) {
        this.handlers.put(path, consumer);
    }
    public void setFallbackHandler(BiConsumer<HttpExchange, Map<String, String>> consumer) {
        if (this.fallback != null) {
            logger.warn("Fallback handler already set!");
            return;
        }
        this.fallback = consumer;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestURI = exchange.getRequestURI().toString();
        URIPath uriPath = URIPath.of(requestURI);

        logger.debug("Handling request: " + requestURI);

        for (URIPath possiblePath : this.handlers.keySet()) {
            if (possiblePath.matches(uriPath)) {
                this.handlers.get(possiblePath).accept(exchange, possiblePath.getURIParams(uriPath));
                return;
            }
        }

        if (this.fallback != null) {
            this.fallback.accept(exchange, new HashMap<>());
            return;
        }

        logger.warn("No handler found for request: " + requestURI);

        byte[] message = ("No handler found for request: " + requestURI).getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(404, message.length);

        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(message);
        outputStream.close();
    }
}
