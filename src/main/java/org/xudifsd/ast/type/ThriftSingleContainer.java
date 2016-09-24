package org.xudifsd.ast.type;

import org.xudifsd.visitor.Visitor;

// include set & list
public class ThriftSingleContainer implements ThriftType {
    public final Type type;
    public final ThriftType innerType;

    public ThriftSingleContainer(Type type, ThriftType innerType) {
        this.type = type;
        this.innerType = innerType;
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }

    public enum Type {
        SET,
        LIST,
    }

    @Override
    public String toString() {
        return "ThriftSingleContainer{" + "type=" + type + ", innerType=" + innerType + '}';
    }
}
