package org.xudifsd.ast;

import org.xudifsd.ast.type.ThriftType;
import org.xudifsd.lexer.Lexer;
import org.xudifsd.lexer.SyntaxException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ThriftMethod extends NamedItem {
    public final ThriftType returnType;

    private List<ThriftField> parList = new ArrayList<>();
    private Set<String> allArgId = new HashSet<>();

    private List<ThriftField> exceptionList = new ArrayList<>();
    private Set<String> allExId = new HashSet<>();

    public ThriftMethod(String name, ThriftType returnType) {
        super(name);
        this.returnType = returnType;
    }

    public void addParameter(Lexer lexer, ThriftField par) throws SyntaxException {
        if (allArgId.contains(par.name)) {
            lexer.throwSyntaxError(String.format("has duplicate parameter id %s in method %s",
                    par.name, super.name));
        }
        allArgId.add(par.name);
        parList.add(par);
    }

    public void addException(Lexer lexer, ThriftField ex) throws SyntaxException {
        if (allExId.contains(ex.name)) {
            lexer.throwSyntaxError(String.format("has duplicate exception id %s in method %s",
                    ex.name, super.name));
        }
        allExId.add(ex.name);
        exceptionList.add(ex);
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
