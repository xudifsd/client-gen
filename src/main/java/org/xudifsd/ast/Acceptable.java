package org.xudifsd.ast;

import org.xudifsd.visitor.Visitor;

public interface Acceptable {
    void accept(Visitor v);
}
