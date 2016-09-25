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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThriftSelfDefinedType that = (ThriftSelfDefinedType) o;

        if (scope != null ? !scope.equals(that.scope) : that.scope != null) return false;
        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        int result = scope != null ? scope.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        if (scope == null) {
            return name;
        } else {
            return scope + "." + name;
        }
    }
}
