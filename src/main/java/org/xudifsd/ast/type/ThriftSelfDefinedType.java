package org.xudifsd.ast.type;

import org.xudifsd.visitor.Visitor;

// include struct & enum
public class ThriftSelfDefinedType implements ThriftType {
    public final String scope; // for included type
    public final String name;

    public ThriftSelfDefinedType(String scope, String name) {
        this.scope = scope;
        this.name = name;
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }

    @Override
    public String toString() {
        return "ThriftSelfDefinedType{" + "name='" + name + '\'' + '}';
    }
}
