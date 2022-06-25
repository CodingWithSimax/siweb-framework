package net.simax_dev.siweb.loader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Loads HTML-Template and makes the css classes unique
 */
public class TemplateCSSLoader {
    private static final Logger logger = LogManager.getLogger(TemplateLoader.class);


    private static int templateCount = 0;
    private static String getID() {
        return "template" + Integer.toHexString(templateCount++);
    }

    private final ClassLoader classLoader;

    private final String[] cssToIgnore = new String[]{
        "head",
        "body"
    };

    public TemplateCSSLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public String renameTemplateCSS(Element body, String cssSources) throws IOException {
        InputStream inputStream = this.classLoader.getResourceAsStream(cssSources);
        assert inputStream != null;
        String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        inputStream.close();

        String id = getID();

        StringBuilder resString = new StringBuilder();

        StringBuilder tempString = new StringBuilder();

        boolean isInStyle = false;
        int lineCount = 1;
        for (char c : content.toCharArray()) {
            if (c == '\n') {
                lineCount++;
            }

            if (c == '{') {
                if (isInStyle) {
                    throw new RuntimeException("CSS-Parsing error detected " + cssSources + ":" + lineCount + " - Unexpected beginning of style");
                }

                String result = this.loadCSSClass(tempString.toString(), id);
                resString.delete(resString.length() - tempString.length(), resString.length());
                resString.append(result);

                tempString = new StringBuilder();

                isInStyle = true;
            }

            resString.append(c);

            if (!isInStyle && c != '\n') {
                tempString.append(c);
            }
            if (c == '}') {
                if (!isInStyle) {
                    throw new RuntimeException("CSS-Parsing error detected " + cssSources + ":" + lineCount + " - Unexpected end of style");
                }
                tempString = new StringBuilder();
                isInStyle = false;
            }

        }

        this.applyHTMLID(body, id);

        return resString.toString();
    }

    private String loadCSSClass(String clazzData, String id) {
        clazzData = clazzData.trim();

        StringBuilder result = new StringBuilder();
        StringBuilder tempData = new StringBuilder();
        for (char c : clazzData.toCharArray()) {
            // All chars that are ignored by a class name
            String separators = " ><";
            if (separators.indexOf(c) != -1) {
                String tempDataString = tempData.toString();
                if (!tempDataString.trim().isEmpty()) {
                    String newSelector = this.handleSelector(tempDataString.trim(), id);

                    result.delete(result.length() - tempDataString.length(), result.length());
                    result.append(newSelector);
                }
                tempData = new StringBuilder();
            } else {
                tempData.append(c);
            }
            result.append(c);
        }
        String newSelector = this.handleSelector(tempData.toString().trim(), id);
        result.delete(result.length() - tempData.length(), result.length());
        result.append(newSelector);

        return result.toString();
    }

    private String handleSelector(String selector, String id) {
        if (!Arrays.asList(this.cssToIgnore).contains(selector)) {
            return selector + "[" + id + "]";
        } else {
            logger.debug("ignoring style for selector '" + selector + "'");
            return selector;
        }
    }

    private void applyHTMLID(Element element, String id) {
        element.attr(id, "");
        for (Element child : element.children()) {
            this.applyHTMLID(child, id);
        }
    }
}
