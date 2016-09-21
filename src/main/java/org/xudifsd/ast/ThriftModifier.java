package org.xudifsd.ast;

public class ThriftModifier {
    public final Type type;

    public enum Type {
        DEFAULT,
        REQUIRED,
        OPTIONAL,
    }

    public ThriftModifier(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ThriftModifier{" + "type=" + type + '}';
    }
}
