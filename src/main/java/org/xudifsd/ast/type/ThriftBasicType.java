package org.xudifsd.ast.type;

import org.xudifsd.visitor.Visitor;

public class ThriftBasicType implements ThriftType {
    public final Type t;

    public enum Type {
        I16,
        I32,
        I64,
        BOOL,
        BYTE,
        DOUBLE,
        STRING,
        BINARY,
        VOID,
    }

    public ThriftBasicType(Type t) {
        this.t = t;
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }

    @Override
    public String toString() {
        return "ThriftBasicType{" + "t=" + t + '}';
    }
}
