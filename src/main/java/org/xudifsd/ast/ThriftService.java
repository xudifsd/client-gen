package org.xudifsd.ast;

import org.xudifsd.lexer.Lexer;
import org.xudifsd.lexer.SyntaxException;

import java.util.HashMap;
import java.util.Map;

public class ThriftService extends NamedItem {
    private Map<String, ThriftMethod> methods = new HashMap<>();

    public ThriftService(String name) {
        super(name);
    }

    public void addMethod(Lexer lexer, ThriftMethod method) throws SyntaxException {
        if (methods.get(method.name) != null) {
            lexer.throwSyntaxError(String.format("%s already defined in service %s",
                    method.name, super.name));
        }
        methods.put(method.name, method);
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
