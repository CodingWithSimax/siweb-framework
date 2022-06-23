package net.simax_dev.siweb.loader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TemplateLoader {
    private final static Logger logger = LogManager.getLogger(TemplateLoader.class);

    private final ClassLoader classLoader;

    public TemplateLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void load(String path) throws IOException {
        logger.debug("Loading template: " + path);

        InputStream inputStream = this.classLoader.getResourceAsStream(path);
        String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        inputStream.close();

        Document document = Jsoup.parse(content);

        logger.debug("Loaded template: " + path);

        for (Element element : document.getElementsByTag("body").get(0).getAllElements()) {

        }
    }
}
