package net.simax_dev.siweb.loader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class TemplateGenerator {
    public String generateTemplate(String baseHTML, String bodyHTML) {
        Document baseHTMLDocument = Jsoup.parse(baseHTML);
        baseHTMLDocument.body().html(bodyHTML);
        return baseHTMLDocument.outerHtml();
    }
}
