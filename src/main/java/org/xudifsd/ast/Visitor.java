package org.xudifsd.ast;

import org.xudifsd.ast.type.ThriftBasicType;
import org.xudifsd.ast.type.ThriftDualContainer;
import org.xudifsd.ast.type.ThriftSelfDefinedType;
import org.xudifsd.ast.type.ThriftSingleContainer;

public interface Visitor {
    void visit(ThriftFile thriftFile);

    void visit(ThriftField thriftField);

    void visit(ThriftStruct thriftStruct);

    void visit(ThriftEnum thriftEnum);

    void visit(ThriftBasicType type);

    void visit(ThriftSelfDefinedType type);

    void visit(ThriftSingleContainer type);

    void visit(ThriftDualContainer type);

    void visit(ThriftException ex);

    void visit(ThriftService service);

    void visit(ThriftMethod method);
}
