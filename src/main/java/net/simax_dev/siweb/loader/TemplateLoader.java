package net.simax_dev.siweb.loader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Loads HTML-Templates and replaces all vars with unique ids and spans
 */
public class TemplateLoader {
    private final static Logger logger = LogManager.getLogger(TemplateLoader.class);

    private final ClassLoader classLoader;
    private final TemplateCSSLoader templateCSSLoader;
    private int varCount = 0;

    public TemplateLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.templateCSSLoader = new TemplateCSSLoader(classLoader);
    }

    private String genRandomVar() {
        return (Integer.toHexString(this.varCount++) + "-" + Long.toHexString(Math.round(Math.random() * 1000000)));
    }

    public TemplateData load(String path, String cssSource) throws IOException {
        logger.debug("Loading template: " + path);

        InputStream inputStream = this.classLoader.getResourceAsStream(path);
        assert inputStream != null;
        String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        inputStream.close();

        Document document = Jsoup.parse(content);

        Map<String, String> vars = this.loadChildren(document.body(), document.body().childNodesCopy());

        String resultCSS = cssSource == null ? null : this.templateCSSLoader.renameTemplateCSS(document.body(), cssSource);

        logger.debug("Loaded template: " + path + " with " + vars.size() + " variables");

        return new TemplateData(document.body().html(), resultCSS);
    }

    public static final class TemplateData {
        private final String html;
        private final String css;

        public TemplateData(String html, String css) {
            this.html = html;
            this.css = css;
        }

        public String getHTML() {
            return this.html;
        }
        public String getCSS() {
            return this.css;
        }
    }

    private Map<String, String> loadChildren(Element parentNode, List<Node> nodes) {
        Map<String, String> vars = new HashMap<>();

        nodes = new ArrayList<>(nodes);
        List<Node> resultNodes = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if (node instanceof TextNode) {
                TextNode textNode = (TextNode) node;
                String content = textNode.text();

                TemplateFilterResult result = this.filterVarsFromContent(content);
                vars.putAll(result.getVars());

                resultNodes.addAll(result.getResultNodes());
                i += result.getResultNodes().size() - 1;
            } else if (node instanceof Element) {
                vars.putAll(this.loadChildren((Element) node, node.childNodesCopy()));
                resultNodes.add(node.clone());
            }
        }

        parentNode.childNodes().forEach(Node::remove);
        parentNode.appendChildren(resultNodes);

        return vars;
    }

    public static class TemplateFilterResult {
        private final List<Node> resultNodes;
        private final Map<String, String> vars;

        public TemplateFilterResult(List<Node> resultNodes, Map<String, String> vars) {
            this.resultNodes = resultNodes;
            this.vars = vars;
        }

        public List<Node> getResultNodes() {
            return this.resultNodes;
        }
        public Map<String, String> getVars() {
            return this.vars;
        }
    }

    private TemplateFilterResult filterVarsFromContent(String content) {
        // TODO Optimize
        // list of all vars and their random codes (-> Vars are being sent obfuscated)
        Map<String, String> vars = new HashMap<>();

        int openCount = 0;
        int closeCount = 0;

        StringBuilder curVar = new StringBuilder();

        List<Node> resultNodes = new ArrayList<>();

        for (char c : content.toCharArray()) {
            if (c == '}') {
                closeCount++;
                if (closeCount >= 2) {
                    closeCount = 0;

                    String varName = curVar.toString();
                    varName = varName.substring(0, varName.length() - 1);

                    curVar = new StringBuilder();

                    Element element = new Element("span");
                    String randomVarName = this.genRandomVar();
                    element.id("var-" + randomVarName);

                    resultNodes.add(element);

                    vars.put(randomVarName, varName);

                    continue;
                }
            } else closeCount = 0;

            curVar.append(c);

            if (c == '{') {
                openCount++;
                if (openCount >= 2) {
                    openCount = 0;
                    curVar.delete(curVar.length()-2, curVar.length());
                    resultNodes.add(new TextNode(curVar.toString()));
                    curVar = new StringBuilder();
                }
            } else openCount = 0;
        }

        resultNodes.add(new TextNode(curVar.toString()));

        return new TemplateFilterResult(resultNodes, vars);
    }
}
