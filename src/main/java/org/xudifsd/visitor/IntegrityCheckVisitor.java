package org.xudifsd.visitor;

import org.xudifsd.ast.ThriftFile;
import org.xudifsd.lexer.SyntaxException;

public class IntegrityCheckVisitor {
    // TODO should implements Visitor

    // TODO also check if included file with same lang.namespace contain same name type

    public void check(ThriftFile file) throws SyntaxException{
        // TODO check if all type has its definition
    }
}
