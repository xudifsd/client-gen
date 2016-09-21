package org.xudifsd.ast.type;

public class ThriftNamespace {
    public final String lang;
    public final String scope;

    public ThriftNamespace(String lang, String scope) {
        this.lang = lang;
        this.scope = scope;
    }

    @Override
    public String toString() {
        return "ThriftNamespace{" + "lang='" + lang + '\'' + ", scope='" + scope + '\'' + '}';
    }
}
