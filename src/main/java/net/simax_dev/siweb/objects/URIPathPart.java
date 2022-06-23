package net.simax_dev.siweb.objects;

public class URIPathPart {
    private final String name;
    private final boolean optional;
    private final boolean variable;

    public URIPathPart(String name) {
        this.variable = name.startsWith(":");
        this.name = this.variable ? name.substring(1) : name;
        this.optional = name.equals("*");
    }

    public String getName() {
        return this.name;
    }

    public boolean isOptional() {
        return this.optional;
    }

    public boolean isVariable() {
        return this.variable;
    }
}
