package org.xudifsd.ast.type;

import org.xudifsd.ast.Acceptable;
import org.xudifsd.visitor.Visitor;

public class ThriftDefaultValue implements Acceptable {
    public final ThriftType type;
    public final String literal;

    public ThriftDefaultValue(ThriftType type, String literal) {
        this.type = type;
        this.literal = literal;
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
