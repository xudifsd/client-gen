package org.xudifsd.ast.type;

import org.xudifsd.ast.Visitor;

public class ThriftDualContainer implements ThriftType {
    public final ThriftType key;
    public final ThriftType value;

    public ThriftDualContainer(ThriftType key, ThriftType value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }

    @Override
    public String toString() {
        return "ThriftDualContainer{" + "key=" + key + ", value=" + value + '}';
    }
}
