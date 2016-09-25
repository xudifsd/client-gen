package org.xudifsd.ast;

import org.xudifsd.ast.type.ThriftDefaultValue;
import org.xudifsd.ast.type.ThriftType;
import org.xudifsd.visitor.Visitor;

// struct, exception, method args, exceptions field
public class ThriftField extends NamedItem {
    public final ThriftModifier modifier;
    public final ThriftType type;
    public final ThriftDefaultValue defaultValue; // null on not set

    public ThriftField(String name, ThriftType type, ThriftModifier modifier, ThriftDefaultValue defaultValue) {
        super(name);
        this.type = type;
        this.modifier = modifier;
        this.defaultValue = defaultValue;
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }

    @Override
    public String toString() {
        return "ThriftField{name=" + name + ", modifier=" + modifier + ", type=" + type
                + ", defaultValue=" + defaultValue + '}';
    }
}
