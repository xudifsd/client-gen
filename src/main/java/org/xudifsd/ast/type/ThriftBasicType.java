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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThriftBasicType that = (ThriftBasicType) o;

        return t == that.t;
    }

    @Override
    public int hashCode() {
        return t != null ? t.hashCode() : 0;
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }

    @Override
    public String toString() {
        switch (t) {
            case I16: return "i16";
            case I32: return "i32";
            case I64: return "i64";
            case BOOL: return "bool";
            case BYTE: return "byte";
            case DOUBLE: return "double";
            case STRING: return "string";
            case BINARY: return "binary";
            case VOID: return "void";
            default: return "unknown type";
        }
    }
}
