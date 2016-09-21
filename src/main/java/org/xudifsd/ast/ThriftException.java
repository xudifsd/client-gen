package org.xudifsd.ast;

import org.xudifsd.lexer.Lexer;
import org.xudifsd.lexer.SyntaxException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ThriftException extends NamedItem {
    private Set<String> allNames = new HashSet<String>();
    private List<ThriftField> allFields = new ArrayList<ThriftField>();

    public ThriftException(String name) {
        super(name);
    }

    public void add(Lexer lexer, ThriftField field) throws SyntaxException {
        if (allNames.contains(field.name)) {
            lexer.throwSyntaxError(field.name + " already exist in struct " + this.name);
        }
        allNames.add(field.name);
        allFields.add(field);
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }

    @Override
    public String toString() {
        return "ThriftException{allFields=" + allFields + '}';
    }
}
