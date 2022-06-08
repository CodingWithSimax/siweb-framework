package net.simax_dev.siweb.loader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TemplateLoader {
    private final ClassLoader classLoader;

    public TemplateLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void load(String path) throws IOException {
        InputStream inputStream = this.classLoader.getResourceAsStream(path);
        String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        inputStream.close();

    }
}
