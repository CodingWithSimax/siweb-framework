package net.simax_dev.siweb.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class URIPath {
    public static URIPath of(String path) {
        if (path.startsWith("/")) path = path.substring(1);

        List<URIPathPart> result = new ArrayList<>();
        for (String s : path.split("/")) {
            result.add(new URIPathPart(s));
        }

        return new URIPath(result);
    }

    private final List<URIPathPart> parts;

    public URIPath(List<URIPathPart> parts) {
        this.parts = parts;
    }

    public boolean matches(URIPath uriPath) {
        if (uriPath.parts.size() != this.parts.size() &&
                !this.parts.get(this.parts.size()-1).getName().equals("**")) return false;

        for (int i = 0; i < this.parts.size(); i++) {
            URIPathPart thisPart = this.parts.get(i);
            URIPathPart uriPart = uriPath.parts.get(i);

            if (thisPart.isOptional() || thisPart.isVariable() || thisPart.getName().equals("**")) continue;

            if (!thisPart.getName().equals(uriPart.getName())) return false;
        }

        return true;
    }

    public Map<String, String> getURIParams(URIPath uriPath) {
        if (uriPath.parts.size() != this.parts.size() &&
            !this.parts.get(this.parts.size()-1).getName().equals("**")) return null;

        Map<String, String> result = new HashMap<>();

        for (int i = 0; i < this.parts.size(); i++) {
            URIPathPart thisPart = this.parts.get(i);
            URIPathPart uriPart = uriPath.parts.get(i);

            if (thisPart.isOptional()) continue;

            if (thisPart.isVariable()) {
                result.put(thisPart.getName(), uriPart.getName());
            }

            if (!thisPart.getName().equals(uriPart.getName())) return null;
        }

        return result;
    }

    public void addURIPart(URIPathPart uriPathPart) {
        this.parts.add(uriPathPart);
    }

    public List<URIPathPart> getParts() {
        return this.parts;
    }

    public String toString() {
        return String.join("/", this.parts.stream().map(URIPathPart::getName).toList());
    }
}
