package org.xudifsd.visitor;

import org.xudifsd.ast.ThriftEnum;
import org.xudifsd.ast.ThriftException;
import org.xudifsd.ast.ThriftField;
import org.xudifsd.ast.ThriftFile;
import org.xudifsd.ast.ThriftMethod;
import org.xudifsd.ast.ThriftService;
import org.xudifsd.ast.ThriftStruct;
import org.xudifsd.ast.type.ThriftBasicType;
import org.xudifsd.ast.type.ThriftDefaultValue;
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

    void visit(ThriftDefaultValue value);
}
