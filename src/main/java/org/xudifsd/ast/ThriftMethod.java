package org.xudifsd.ast;

import org.xudifsd.ast.type.ThriftType;
import org.xudifsd.lexer.Lexer;
import org.xudifsd.lexer.SyntaxException;
import org.xudifsd.visitor.Visitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ThriftMethod extends NamedItem {
    public final ThriftType returnType;

    private List<ThriftField> parList = new ArrayList<ThriftField>();
    private Set<String> allArgId = new HashSet<String>();

    private List<ThriftField> exceptionList = new ArrayList<ThriftField>();
    private Set<String> allExId = new HashSet<String>();

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

    public List<ThriftField> getParList() {
        return parList;
    }

    public List<ThriftField> getExceptionList() {
        return exceptionList;
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
