package org.xudifsd.ast;

public abstract class NamedItem implements Acceptable {
    public final String name;

    public NamedItem(String name) {
        this.name = name;
    }
}
